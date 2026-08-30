package com.universidad.academico.config;

import com.universidad.academico.domain.BoletaInscripcion;
import com.universidad.academico.domain.DiaSemana;
import com.universidad.academico.domain.Docente;
import com.universidad.academico.domain.Estudiante;
import com.universidad.academico.domain.Grupo;
import com.universidad.academico.domain.Horario;
import com.universidad.academico.domain.Materia;
import com.universidad.academico.repository.BoletaInscripcionRepository;
import com.universidad.academico.repository.DocenteRepository;
import com.universidad.academico.repository.EstudianteRepository;
import com.universidad.academico.repository.GrupoRepository;
import com.universidad.academico.repository.MateriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Set;

/**
 * Seeder de datos base para el microservicio academico.
 * Precarga docentes, estudiantes, materias, grupos con horarios y boletas inscritas.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AcademicoDataSeeder implements CommandLineRunner {

    private final DocenteRepository docenteRepository;
    private final EstudianteRepository estudianteRepository;
    private final MateriaRepository materiaRepository;
    private final GrupoRepository grupoRepository;
    private final BoletaInscripcionRepository boletaRepository;

    @Override
    public void run(String... args) {
        if (docenteRepository.count() > 0) {
            log.info("Los datos academicos ya estan inicializados en la base de datos");
            return;
        }

        // 1. Crear Docentes
        Docente docente1 = docenteRepository.save(Docente.builder()
                .codigo("DOC-101")
                .apellidos("Mendoza Ramos")
                .nombre("Roberto")
                .telefono("70011223")
                .correo("roberto.mendoza@universidad.edu")
                .build());

        Docente docente2 = docenteRepository.save(Docente.builder()
                .codigo("DOC-102")
                .apellidos("Fernandez Silva")
                .nombre("Claudia")
                .telefono("70044556")
                .correo("claudia.fernandez@universidad.edu")
                .build());

        // 2. Crear Materias
        Materia materia1 = materiaRepository.save(Materia.builder()
                .sigla("INF412")
                .nombre("Arquitectura de Software")
                .build());

        Materia materia2 = materiaRepository.save(Materia.builder()
                .sigla("INF310")
                .nombre("Sistemas Operativos")
                .build());

        // 3. Crear Estudiantes
        Estudiante est1 = estudianteRepository.save(Estudiante.builder()
                .registro("2024001")
                .apellidos("Perez Gomez")
                .nombre("Juan")
                .carrera("Ingenieria Informatica")
                .plan("2020")
                .telefono("78912345")
                .correo("juan.perez@estudiante.edu")
                .build());

        Estudiante est2 = estudianteRepository.save(Estudiante.builder()
                .registro("2024002")
                .apellidos("Garcia Lopez")
                .nombre("Maria")
                .carrera("Ingenieria Informatica")
                .plan("2020")
                .telefono("78965432")
                .correo("maria.garcia@estudiante.edu")
                .build());

        Estudiante est3 = estudianteRepository.save(Estudiante.builder()
                .registro("2024003")
                .apellidos("Suarez Rojas")
                .nombre("Carlos")
                .carrera("Ingenieria en Sistemas")
                .plan("2020")
                .telefono("78998877")
                .correo("carlos.suarez@estudiante.edu")
                .build());

        // 4. Crear Grupos con Horarios
        Grupo grupo1 = Grupo.builder()
                .nombre("SC")
                .cupo(40)
                .materia(materia1)
                .docente(docente1)
                .horarios(new ArrayList<>())
                .build();

        grupo1.getHorarios().add(Horario.builder()
                .dia(DiaSemana.LUNES)
                .horaInicio(LocalTime.of(7, 0))
                .horaFin(LocalTime.of(9, 15))
                .grupo(grupo1)
                .build());

        grupo1.getHorarios().add(Horario.builder()
                .dia(DiaSemana.MIERCOLES)
                .horaInicio(LocalTime.of(7, 0))
                .horaFin(LocalTime.of(9, 15))
                .grupo(grupo1)
                .build());

        Grupo grupoGuardado1 = grupoRepository.save(grupo1);

        Grupo grupo2 = Grupo.builder()
                .nombre("SA")
                .cupo(35)
                .materia(materia2)
                .docente(docente2)
                .horarios(new ArrayList<>())
                .build();

        grupo2.getHorarios().add(Horario.builder()
                .dia(DiaSemana.MARTES)
                .horaInicio(LocalTime.of(9, 15))
                .horaFin(LocalTime.of(11, 30))
                .grupo(grupo2)
                .build());

        grupo2.getHorarios().add(Horario.builder()
                .dia(DiaSemana.JUEVES)
                .horaInicio(LocalTime.of(9, 15))
                .horaFin(LocalTime.of(11, 30))
                .grupo(grupo2)
                .build());

        Grupo grupoGuardado2 = grupoRepository.save(grupo2);

        // 5. Crear Boletas de Inscripcion
        boletaRepository.save(BoletaInscripcion.builder()
                .fecha(LocalDate.now())
                .hora(LocalTime.now())
                .gestion("2-2024")
                .estudiante(est1)
                .grupos(Set.of(grupoGuardado1, grupoGuardado2))
                .build());

        boletaRepository.save(BoletaInscripcion.builder()
                .fecha(LocalDate.now())
                .hora(LocalTime.now())
                .gestion("2-2024")
                .estudiante(est2)
                .grupos(Set.of(grupoGuardado1))
                .build());

        log.info("Datos semilla academicos cargados exitosamente (Docentes, Materias, Estudiantes, Grupos, Horarios y Boletas)");
    }
}
