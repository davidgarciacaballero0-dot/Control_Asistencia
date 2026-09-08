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
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementacion del servicio de importacion masiva de estudiantes (CSV / Excel / PDF).
 * Aplica Clean Architecture, desacoplamiento con auth-service via OpenFeign
 * y soporte para sincronizacion dinamica con baja logica y extraccion de fotografias.
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
        return importarEstudiantesAGrupo(grupoId, archivo, null);
    }

    @Override
    @Transactional
    public CargaMasivaEstudiantesDto importarEstudiantesAGrupo(Long grupoId, MultipartFile archivo, MultipartFile archivoPdf) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo academico no encontrado con ID: " + grupoId));

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo proporcionado esta vacio");
        }

        String originalFilename = archivo.getOriginalFilename();
        String nombreArchivo = (originalFilename != null) ? originalFilename.toLowerCase() : "";
        List<Map<String, String>> filas;

        // 1. Extraccion de fotografias si se proporciono archivo PDF
        List<String> fotosBase64 = new ArrayList<>();
        try {
            if (archivoPdf != null && !archivoPdf.isEmpty()) {
                fotosBase64 = extraerFotosDePdf(archivoPdf.getInputStream());
                log.info("Se extrajeron {} fotografias del archivo PDF adjunto", fotosBase64.size());
            } else if (nombreArchivo.endsWith(".pdf")) {
                fotosBase64 = extraerFotosDePdf(archivo.getInputStream());
                log.info("Se extrajeron {} fotografias del archivo PDF principal", fotosBase64.size());
            }
        } catch (Exception e) {
            log.error("Advertencia: No se pudieron extraer fotos del PDF: {}", e.getMessage());
        }

        // 2. Parseo del archivo tabular de estudiantes
        try {
            if (nombreArchivo.endsWith(".xlsx") || nombreArchivo.endsWith(".xls")) {
                filas = parsearExcel(archivo.getInputStream());
            } else if (nombreArchivo.endsWith(".pdf")) {
                filas = parsearPdf(archivo.getInputStream());
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
        int fotosAsignadas = 0;
        List<String> errores = new ArrayList<>();
        List<EstudianteDto> estudiantesProcesados = new ArrayList<>();
        Set<String> registrosEnNuevoArchivo = new HashSet<>();

        String gestionActual = LocalDate.now().getYear() + "-" + (LocalDate.now().getMonthValue() <= 6 ? "1" : "2");

        for (int i = 0; i < filas.size(); i++) {
            Map<String, String> fila = filas.get(i);
            int numeroFila = i + 2; // Considerando cabecera en fila 1

            String registro = obtenerValor(fila, "registro", "reg", "nro_registro", "codigo")
                    .replaceAll("[^a-zA-Z0-9_-]", "")
                    .trim();
            if (registro.isBlank()) {
                errores.add("Fila " + numeroFila + ": Numero de registro no especificado");
                fallidos++;
                continue;
            }

            try {
                String ciOriginal = obtenerValor(fila, "ci", "cedula", "documento", "dni")
                        .replaceAll("[^a-zA-Z0-9_-]", "")
                        .trim();

                String apellidos = obtenerValor(fila, "apellidos", "apellido", "apellidos_completos")
                        .replaceAll("\"", "")
                        .trim();
                String nombre = obtenerValor(fila, "nombre", "nombres", "nombre_completo")
                        .replaceAll("\"", "")
                        .trim();

                // Manejo de columna combinada "Apellidos y Nombres"
                if (apellidos.isBlank() && nombre.isBlank()) {
                    String nombreCompleto = obtenerValor(fila, "apellidos_y_nombres", "nombre_completo", "estudiante")
                            .replaceAll("\"", "")
                            .trim();
                    if (!nombreCompleto.isBlank()) {
                        String[] partes = nombreCompleto.split("\\s+");
                        if (partes.length >= 4) {
                            apellidos = partes[0] + " " + partes[1];
                            StringBuilder sbNombre = new StringBuilder();
                            for (int k = 2; k < partes.length; k++) {
                                if (!sbNombre.isEmpty()) sbNombre.append(" ");
                                sbNombre.append(partes[k]);
                            }
                            nombre = sbNombre.toString();
                        } else if (partes.length == 3) {
                            apellidos = partes[0] + " " + partes[1];
                            nombre = partes[2];
                        } else if (partes.length == 2) {
                            apellidos = partes[0];
                            nombre = partes[1];
                        } else {
                            apellidos = nombreCompleto;
                            nombre = "Estudiante";
                        }
                    } else {
                        apellidos = "Estudiante";
                        nombre = registro;
                    }
                }

                String carrera = obtenerValor(fila, "carrera", "carr", "programa");
                if (carrera.isBlank()) {
                    carrera = "187 - Ingenieria Informatica";
                }

                String plan = obtenerValor(fila, "plan", "plan_estudios", "plan_academico");
                if (plan.isBlank()) {
                    plan = "Plan 2020";
                }

                String telefono = obtenerValor(fila, "telefono", "tel", "telcel", "celular", "movil");
                String correo = obtenerValor(fila, "email", "correo", "correo_electronico");
                if (correo.isBlank()) {
                    correo = registro + "@universidad.edu";
                }

                // Generar contrasena inicial de acceso:
                // Si viene CI, usar CI. Si no viene CI, Iniciales en mayusculas + Registro (ej: GCD217058795)
                String contrasenaInicial = generarContrasenaInicial(apellidos, nombre, registro, ciOriginal);
                String ciEstudiante = (!ciOriginal.isBlank()) ? ciOriginal : contrasenaInicial;

                // Obtener foto en Base64 asociada por posicion secuencial
                String fotoBase64 = null;
                if (i < fotosBase64.size()) {
                    fotoBase64 = fotosBase64.get(i);
                }

                // 1. Guardar o actualizar entidad Estudiante
                Estudiante estudiante = estudianteRepository.findById(registro).orElse(null);
                if (estudiante == null) {
                    estudiante = Estudiante.builder()
                            .registro(registro)
                            .ci(ciEstudiante)
                            .apellidos(apellidos)
                            .nombre(nombre)
                            .carrera(carrera)
                            .plan(plan)
                            .telefono(telefono)
                            .correo(correo)
                            .fotoBase64(fotoBase64)
                            .build();
                    creados++;
                } else {
                    estudiante.setCi(ciEstudiante);
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
                    if (fotoBase64 != null && !fotoBase64.isBlank()) {
                        estudiante.setFotoBase64(fotoBase64);
                    }
                    actualizados++;
                }

                if (estudiante.getFotoBase64() != null && !estudiante.getFotoBase64().isBlank()) {
                    fotosAsignadas++;
                }

                Estudiante guardado = estudianteRepository.save(estudiante);
                registrosEnNuevoArchivo.add(guardado.getRegistro());

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

                // 3. Aprovisionar credenciales en auth-service (Usuario = Registro, Contrasena = contrasenaInicial)
                try {
                    authFeignClient.provisionarUsuario(ProvisionarUsuarioRequest.builder()
                            .username(guardado.getRegistro())
                            .password(contrasenaInicial)
                            .email(guardado.getCorreo())
                            .nombreCompleto(guardado.getNombre() + " " + guardado.getApellidos())
                            .ci(contrasenaInicial)
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

        // 4. Sincronizacion y Baja Logica: Desvincular del grupo a estudiantes que ya no figuran en la lista
        int totalBajas = 0;
        List<BoletaInscripcion> boletasDelGrupo = boletaRepository.findBoletasByGrupoId(grupo.getId());
        for (BoletaInscripcion boletaExistente : boletasDelGrupo) {
            String regEstudiante = boletaExistente.getEstudiante().getRegistro();
            if (!registrosEnNuevoArchivo.contains(regEstudiante)) {
                boolean removido = boletaExistente.getGrupos().removeIf(g -> g.getId().equals(grupo.getId()));
                if (removido) {
                    boletaRepository.save(boletaExistente);
                    totalBajas++;
                    log.info("Baja logica aplicada: Estudiante {} desvinculado del Grupo #{}", regEstudiante, grupo.getId());
                }
            }
        }

        log.info("Importacion completada para Grupo #{}: {} procesados, {} creados, {} actualizados, {} inscritos, {} bajas logicas, {} fotos asignadas, {} fallidos",
                grupoId, procesados, creados, actualizados, inscritos, totalBajas, fotosAsignadas, fallidos);

        return CargaMasivaEstudiantesDto.builder()
                .grupoId(grupo.getId())
                .grupoNombre(grupo.getNombre())
                .materiaSigla(grupo.getMateria() != null ? grupo.getMateria().getSigla() : "")
                .materiaNombre(grupo.getMateria() != null ? grupo.getMateria().getNombre() : "")
                .totalProcesados(procesados)
                .totalNuevos(creados)
                .totalActualizados(actualizados)
                .totalInscritos(inscritos)
                .totalBajasLogicas(totalBajas)
                .totalFotosProcesadas(fotosAsignadas)
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
     * Extrae todas las fotografias contenidas en un archivo PDF y las retorna en formato Base64.
     */
    private List<String> extraerFotosDePdf(InputStream pdfInputStream) {
        List<String> fotos = new ArrayList<>();
        try {
            byte[] bytes = pdfInputStream.readAllBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                for (PDPage page : document.getPages()) {
                    PDResources resources = page.getResources();
                    if (resources == null) continue;

                    for (COSName xObjectName : resources.getXObjectNames()) {
                        PDXObject xObject = resources.getXObject(xObjectName);
                        if (xObject instanceof PDImageXObject image) {
                            BufferedImage bImage = image.getImage();
                            if (bImage != null) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ImageIO.write(bImage, "png", baos);
                                String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
                                fotos.add(base64);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error al extraer fotografias desde el archivo PDF", e);
        }
        return fotos;
    }

    /**
     * Parsea lineas tabulares de texto desde un archivo PDF oficial.
     */
    private List<Map<String, String>> parsearPdf(InputStream inputStream) throws Exception {
        List<Map<String, String>> filas = new ArrayList<>();
        byte[] bytes = inputStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            String[] lineas = text.split("\\r?\\n");
            for (String linea : lineas) {
                linea = linea.trim();
                // Patrones tipicos: Nro Registro Apellidos_y_Nombres Carr Plan Tel/Cel Email
                if (linea.matches("^\\d+\\s+\\d{7,10}\\s+.*")) {
                    String[] partes = linea.split("\\s+");
                    if (partes.length >= 6) {
                        String registro = partes[1];
                        String email = "";
                        String telefono = "";
                        String plan = "3";
                        String carrera = "187";
                        int indexFinNombre = partes.length - 1;

                        if (partes[partes.length - 1].contains("@")) {
                            email = partes[partes.length - 1];
                            indexFinNombre--;
                        }
                        if (indexFinNombre >= 2 && partes[indexFinNombre].matches("\\d{7,10}")) {
                            telefono = partes[indexFinNombre];
                            indexFinNombre--;
                        }
                        if (indexFinNombre >= 2 && partes[indexFinNombre].matches("\\d+")) {
                            plan = partes[indexFinNombre];
                            indexFinNombre--;
                        }
                        if (indexFinNombre >= 2 && partes[indexFinNombre].matches("\\d+")) {
                            carrera = partes[indexFinNombre];
                            indexFinNombre--;
                        }

                        StringBuilder nombreCompleto = new StringBuilder();
                        for (int k = 2; k <= indexFinNombre; k++) {
                            if (!nombreCompleto.isEmpty()) nombreCompleto.append(" ");
                            nombreCompleto.append(partes[k]);
                        }

                        Map<String, String> fila = new HashMap<>();
                        fila.put("registro", registro);
                        fila.put("apellidos_y_nombres", nombreCompleto.toString());
                        fila.put("carrera", carrera);
                        fila.put("plan", plan);
                        fila.put("telefono", telefono);
                        fila.put("email", email);
                        filas.add(fila);
                    }
                }
            }
        }
        return filas;
    }

    /**
     * Parsea un archivo CSV delimitado por coma, punto y coma o tabulacion,
     * soportando registros encapsulados con comillas dobles y escapado de Excel.
     */
    private List<Map<String, String>> parsearCsv(InputStream inputStream) throws Exception {
        List<Map<String, String>> filas = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String lineaCabecera = reader.readLine();
            if (lineaCabecera == null) return filas;

            // Manejo de BOM UTF-8
            if (lineaCabecera.startsWith("\uFEFF")) {
                lineaCabecera = lineaCabecera.substring(1);
            }

            lineaCabecera = desescaparLineaCsvSiEsNecesario(lineaCabecera);

            // Detectar delimitador
            String delimitador = ",";
            if (lineaCabecera.contains(";")) {
                delimitador = ";";
            } else if (lineaCabecera.contains("\t")) {
                delimitador = "\t";
            }

            List<String> cabecerasRaw = dividirLineaCsv(lineaCabecera, delimitador);
            String[] cabeceras = normalizarCabeceras(cabecerasRaw.toArray(new String[0]));

            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                linea = desescaparLineaCsvSiEsNecesario(linea);
                List<String> valores = dividirLineaCsv(linea, delimitador);
                Map<String, String> fila = new HashMap<>();
                for (int i = 0; i < cabeceras.length && i < valores.size(); i++) {
                    fila.put(cabeceras[i], limpiarValor(valores.get(i)));
                }
                filas.add(fila);
            }
        }
        return filas;
    }

    /**
     * Desescapa lineas que provienen de exportaciones de Excel donde la fila entera
     * fue encapsulada entre comillas dobles y las comillas internas fueron duplicadas.
     */
    private String desescaparLineaCsvSiEsNecesario(String linea) {
        if (linea == null) return "";
        String l = linea.trim();
        if (l.startsWith("\"") && l.endsWith("\"") && l.contains("\"\"")) {
            l = l.substring(1, l.length() - 1);
            l = l.replace("\"\"", "\"");
        }
        return l;
    }

    /**
     * Divide una linea CSV respetando comillas y delimitadores.
     */
    private List<String> dividirLineaCsv(String linea, String delimitador) {
        List<String> resultado = new ArrayList<>();
        char delim = delimitador.charAt(0);
        StringBuilder actual = new StringBuilder();
        boolean enComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '\"') {
                enComillas = !enComillas;
            } else if (c == delim && !enComillas) {
                resultado.add(actual.toString());
                actual.setLength(0);
                continue;
            }
            actual.append(c);
        }
        resultado.add(actual.toString());
        return resultado;
    }

    /**
     * Remueve comillas circundantes y espacios en blanco de un valor de celda.
     */
    private String limpiarValor(String val) {
        if (val == null) return "";
        String s = val.trim();
        while (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1).trim();
        }
        return s;
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
                return limpiarValor(val);
            }
        }
        return "";
    }

    /**
     * Genera la contrasena inicial de acceso para el estudiante.
     * Si el archivo incluye CI, se utiliza el CI directamente.
     * Si no incluye CI, se genera combinando las iniciales del nombre completo en mayusculas
     * con el numero de registro academico (Ejemplo: GARCIA CABALLERO DAVID con registro 217058795 -> GCD217058795).
     */
    private String generarContrasenaInicial(String apellidos, String nombre, String registro, String ci) {
        if (ci != null && !ci.trim().isEmpty()) {
            return ci.trim();
        }

        StringBuilder sb = new StringBuilder();
        String nombreCompleto = (apellidos != null ? apellidos : "") + " " + (nombre != null ? nombre : "");
        String[] palabras = nombreCompleto.trim().split("\\s+");
        for (String palabra : palabras) {
            String limpia = palabra.replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ]", "");
            if (!limpia.isEmpty()) {
                sb.append(Character.toUpperCase(limpia.charAt(0)));
            }
        }

        String iniciales = sb.toString();
        if (iniciales.isEmpty()) {
            iniciales = "EST";
        }

        return iniciales + (registro != null ? registro.trim() : "");
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
                .fotoBase64(e.getFotoBase64())
                .build();
    }
}
