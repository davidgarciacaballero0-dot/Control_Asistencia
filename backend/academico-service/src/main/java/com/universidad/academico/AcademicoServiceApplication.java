package com.universidad.academico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Clase principal de inicio para el microservicio academico (academico-service).
 * Gestiona estudiantes, docentes, materias, grupos, horarios e inscripciones en el puerto 8081.
 */
@SpringBootApplication
@EnableFeignClients
public class AcademicoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcademicoServiceApplication.class, args);
    }
}
