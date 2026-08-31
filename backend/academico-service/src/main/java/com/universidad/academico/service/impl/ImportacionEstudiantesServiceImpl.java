package com.universidad.academico.service.impl;

import com.universidad.academico.client.AuthFeignClient;
import com.universidad.academico.client.dto.ProvisionarUsuarioRequest;
import com.universidad.academico.domain.BoletaInscripcion;
import com.universidad.academico.domain.Estudiante;
import com.universidad.academico.domain.Grupo;
import com.universidad.academico.dto.CargaMasivaEstudiantesDto;
import com.universidad.academico.dto.EstudianteDto;
import com.universidad.academico.repository.BoletaInscripcionRepository;
import com.universidad.academico.repository.EstudianteRepository;
import com.universidad.academico.repository.GrupoRepository;
import com.universidad.academico.service.ImportacionEstudiantesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Implementacion del servicio de importacion masiva de estudiantes (CSV / Excel).
 * Aplica Clean Architecture y desacoplamiento con auth-service via OpenFeign.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportacionEstudiantesServiceImpl implements ImportacionEstudiantesService {

    private final GrupoRepository grupoRepository;
    private final EstudianteRepository estudianteRepository;
    private final BoletaInscripcionRepository boletaRepository;
    private final AuthFeignClient authFeignClient;

    @Override
    @Transactional
    public CargaMasivaEstudiantesDto importarEstudiantesAGrupo(Long grupoId, MultipartFile archivo) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo academico no encontrado con ID: " + grupoId));

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo proporcionado esta vacio");
        }

        String originalFilename = archivo.getOriginalFilename();
        String nombreArchivo = (originalFilename != null) ? originalFilename.toLowerCase() : "";
        List<Map<String, String>> filas;

        try {
            if (nombreArchivo.endsWith(".xlsx") || nombreArchivo.endsWith(".xls")) {
                filas = parsearExcel(archivo.getInputStream());
            } else {
                filas = parsearCsv(archivo.getInputStream());
            }
        } catch (Exception e) {
            log.error("Error al parsear archivo de estudiantes", e);
            throw new IllegalArgumentException("Error al procesar el archivo: " + e.getMessage());
        }

        int procesados = 0;
        int creados = 0;
        int actualizados = 0;
        int inscritos = 0;
        int fallidos = 0;
        List<String> errores = new ArrayList<>();
        List<EstudianteDto> estudiantesProcesados = new ArrayList<>();

        String gestionActual = LocalDate.now().getYear() + "-" + (LocalDate.now().getMonthValue() <= 6 ? "1" : "2");

        for (int i = 0; i < filas.size(); i++) {
            Map<String, String> fila = filas.get(i);
            int numeroFila = i + 2; // Considerando cabecera en fila 1

            String registro = obtenerValor(fila, "registro", "reg", "nro_registro", "codigo");
            if (registro.isBlank()) {
                errores.add("Fila " + numeroFila + ": Numero de registro no especificado");
                fallidos++;
                continue;
            }

            try {
                String ci = obtenerValor(fila, "ci", "cedula", "documento", "dni");
                if (ci.isBlank()) {
                    ci = registro; // Fallback al registro si no tiene CI
                }

                String apellidos = obtenerValor(fila, "apellidos", "apellido", "apellidos_completos");
                String nombre = obtenerValor(fila, "nombre", "nombres", "nombre_completo");
                if (apellidos.isBlank() && nombre.isBlank()) {
                    apellidos = "Estudiante";
                    nombre = registro;
                }

                String carrera = obtenerValor(fila, "carrera", "carr", "programa");
                if (carrera.isBlank()) {
                    carrera = "187 - Ingenieria Informatica";
                }

                String plan = obtenerValor(fila, "plan", "plan_estudios", "plan_academico");
                if (plan.isBlank()) {
                    plan = "Plan 2020";
                }

                String telefono = obtenerValor(fila, "telefono", "tel", "celular", "movil");
                String correo = obtenerValor(fila, "email", "correo", "correo_electronico");
                if (correo.isBlank()) {
                    correo = registro + "@universidad.edu";
                }

                // 1. Guardar o actualizar entidad Estudiante
                boolean esNuevo = false;
                Estudiante estudiante = estudianteRepository.findById(registro).orElse(null);
                if (estudiante == null) {
                    esNuevo = true;
                    estudiante = Estudiante.builder()
                            .registro(registro)
                            .ci(ci)
                            .apellidos(apellidos)
                            .nombre(nombre)
                            .carrera(carrera)
                            .plan(plan)
                            .telefono(telefono)
                            .correo(correo)
                            .build();
                    creados++;
                } else {
                    estudiante.setCi(ci);
                    estudiante.setApellidos(apellidos);
                    estudiante.setNombre(nombre);
                    estudiante.setCarrera(carrera);
                    estudiante.setPlan(plan);
                    if (!telefono.isBlank()) {
                        estudiante.setTelefono(telefono);
                    }
                    if (!correo.isBlank()) {
                        estudiante.setCorreo(correo);
                    }
                    actualizados++;
                }

                Estudiante guardado = estudianteRepository.save(estudiante);

                // 2. Asociar / Inscribir al Grupo mediante BoletaInscripcion
                BoletaInscripcion boleta = boletaRepository
                        .findByEstudianteRegistroAndGestion(guardado.getRegistro(), gestionActual)
                        .orElseGet(() -> BoletaInscripcion.builder()
                                .estudiante(guardado)
                                .gestion(gestionActual)
                                .fecha(LocalDate.now())
                                .hora(java.time.LocalTime.now())
                                .grupos(new HashSet<>())
                                .build());

                if (boleta.getGrupos() == null) {
                    boleta.setGrupos(new HashSet<>());
                }

                boolean yaInscrito = boleta.getGrupos().stream().anyMatch(g -> g.getId().equals(grupo.getId()));
                if (!yaInscrito) {
                    boleta.getGrupos().add(grupo);
                    boletaRepository.save(boleta);
                    inscritos++;
                }

                // 3. Aprovisionar credenciales en auth-service (Usuario = Registro, Contrasena = CI)
                try {
                    authFeignClient.provisionarUsuario(ProvisionarUsuarioRequest.builder()
                            .username(guardado.getRegistro())
                            .password(guardado.getCi())
                            .email(guardado.getCorreo())
                            .nombreCompleto(guardado.getNombre() + " " + guardado.getApellidos())
                            .ci(guardado.getCi())
                            .rol("ROLE_ESTUDIANTE")
                            .identificadorReferencia(guardado.getRegistro())
                            .build());
                } catch (Exception authEx) {
                    log.warn("No se pudo aprovisionar credenciales en auth-service para {}: {}", guardado.getRegistro(), authEx.getMessage());
                }

                procesados++;
                estudiantesProcesados.add(mapearAEstudianteDto(guardado));

            } catch (Exception ex) {
                log.error("Error al procesar estudiante registro {}: {}", registro, ex.getMessage());
                errores.add("Fila " + numeroFila + " (" + registro + "): " + ex.getMessage());
                fallidos++;
            }
        }

        log.info("Importacion completada para Grupo #{}: {} procesados, {} creados, {} actualizados, {} inscritos, {} fallidos",
                grupoId, procesados, creados, actualizados, inscritos, fallidos);

        return CargaMasivaEstudiantesDto.builder()
                .grupoId(grupo.getId())
                .grupoNombre(grupo.getNombre())
                .materiaSigla(grupo.getMateria() != null ? grupo.getMateria().getSigla() : "")
                .materiaNombre(grupo.getMateria() != null ? grupo.getMateria().getNombre() : "")
                .totalProcesados(procesados)
                .totalNuevos(creados)
                .totalActualizados(actualizados)
                .totalInscritos(inscritos)
                .totalFallidos(fallidos)
                .errores(errores)
                .estudiantes(estudiantesProcesados)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteDto> listarEstudiantesPorGrupo(Long grupoId) {
        return boletaRepository.findEstudiantesByGrupoId(grupoId).stream()
                .map(this::mapearAEstudianteDto)
                .toList();
    }

    /**
     * Parsea un archivo CSV delimitado por coma, punto y coma o tabulacion.
     */
    private List<Map<String, String>> parsearCsv(InputStream inputStream) throws Exception {
        List<Map<String, String>> filas = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String lineaCabecera = reader.readLine();
            if (lineaCabecera == null) return filas;

            // Detectar delimitador
            String delimitador = ",";
            if (lineaCabecera.contains(";")) {
                delimitador = ";";
            } else if (lineaCabecera.contains("\t")) {
                delimitador = "\t";
            }

            String[] cabeceras = normalizarCabeceras(lineaCabecera.split(delimitador));

            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] valores = linea.split(delimitador, -1);
                Map<String, String> fila = new HashMap<>();
                for (int i = 0; i < cabeceras.length && i < valores.length; i++) {
                    fila.put(cabeceras[i], valores[i].trim().replaceAll("^\"|\"$", ""));
                }
                filas.add(fila);
            }
        }
        return filas;
    }

    /**
     * Parsea un libro Excel (.xlsx o .xls) usando Apache POI.
     */
    private List<Map<String, String>> parsearExcel(InputStream inputStream) throws Exception {
        List<Map<String, String>> filas = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                return filas;
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return filas;

            List<String> cabecerasList = new ArrayList<>();
            for (Cell cell : headerRow) {
                cabecerasList.add(formatter.formatCellValue(cell).trim());
            }

            String[] cabeceras = normalizarCabeceras(cabecerasList.toArray(new String[0]));

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Map<String, String> fila = new HashMap<>();
                boolean tieneContenido = false;

                for (int c = 0; c < cabeceras.length; c++) {
                    Cell cell = row.getCell(c);
                    String valor = (cell != null) ? formatter.formatCellValue(cell).trim() : "";
                    if (!valor.isEmpty()) tieneContenido = true;
                    fila.put(cabeceras[c], valor);
                }

                if (tieneContenido) {
                    filas.add(fila);
                }
            }
        }
        return filas;
    }

    private String[] normalizarCabeceras(String[] raw) {
        String[] normalizadas = new String[raw.length];
        for (int i = 0; i < raw.length; i++) {
            String col = raw[i].trim().toLowerCase()
                    .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                    .replace("ñ", "n").replace(" ", "_").replaceAll("[^a-z0-9_]", "");
            normalizadas[i] = col;
        }
        return normalizadas;
    }

    private String obtenerValor(Map<String, String> fila, String... llaves) {
        for (String llave : llaves) {
            String val = fila.get(llave);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        return "";
    }

    private EstudianteDto mapearAEstudianteDto(Estudiante e) {
        return EstudianteDto.builder()
                .registro(e.getRegistro())
                .ci(e.getCi())
                .apellidos(e.getApellidos())
                .nombre(e.getNombre())
                .carrera(e.getCarrera())
                .plan(e.getPlan())
                .telefono(e.getTelefono())
                .correo(e.getCorreo())
                .build();
    }
}
