package com.universidad.academico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de inicio para el microservicio academico (academico-service).
 * Gestiona estudiantes, docentes, materias, grupos, horarios e inscripciones en el puerto 8081.
 */
@SpringBootApplication
public class AcademicoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcademicoServiceApplication.class, args);
    }
}
