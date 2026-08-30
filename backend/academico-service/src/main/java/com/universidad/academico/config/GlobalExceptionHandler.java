package com.universidad.academico.config;

import com.universidad.academico.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Controlador global de excepciones para el microservicio academico.
 * Captura y estandariza las excepciones producidas durante la ejecucion de peticiones REST.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validacion en los DTOs de entrada anotados con @Valid.
     *
     * @param ex Excepcion de validacion.
     * @param request Peticion HTTP.
     * @return Respuesta estructurada con codigo 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> manejarValidacion(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String mensajeErrores = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("Error de validacion en academico-service para {}: {}", request.getRequestURI(), mensajeErrores);

        ErrorResponseDto errorDto = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validacion Fallida")
                .mensaje(mensajeErrores)
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    /**
     * Maneja excepciones de tipo IllegalArgumentException en la capa de servicios academicos.
     *
     * @param ex Excepcion de argumento ilegal.
     * @param request Peticion HTTP.
     * @return Respuesta estructurada con codigo 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> manejarArgumentoInvalido(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Argumento invalido en academico-service para {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponseDto errorDto = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Solicitud Invalida")
                .mensaje(ex.getMessage())
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    /**
     * Maneja excepciones de tipo IllegalStateException en la capa de servicios academicos.
     *
     * @param ex Excepcion de estado ilegal.
     * @param request Peticion HTTP.
     * @return Respuesta estructurada con codigo 409 Conflict.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> manejarEstadoInvalido(
            IllegalStateException ex, HttpServletRequest request) {

        log.warn("Conflicto de estado en academico-service para {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponseDto errorDto = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflicto de Estado")
                .mensaje(ex.getMessage())
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDto);
    }

    /**
     * Maneja cualquier excepcion generica imprevista.
     *
     * @param ex Excepcion generica.
     * @param request Peticion HTTP.
     * @return Respuesta estructurada con codigo 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> manejarExcepcionGenerica(
            Exception ex, HttpServletRequest request) {

        log.error("Excepcion no controlada en academico-service para {}: ", request.getRequestURI(), ex);

        ErrorResponseDto errorDto = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error Interno del Servidor")
                .mensaje("Ha ocurrido un error interno en el servicio academico. Por favor intente mas tarde.")
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
    }
}
