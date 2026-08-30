package com.universidad.asistencia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Clase principal de inicio para el microservicio de control de asistencia (asistencia-service).
 * Gestiona sesiones de clase, generacion y validacion de codigos QR, e invocaciones OpenFeign al puerto 8082.
 */
@SpringBootApplication
@EnableFeignClients
public class AsistenciaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsistenciaServiceApplication.class, args);
    }
}
