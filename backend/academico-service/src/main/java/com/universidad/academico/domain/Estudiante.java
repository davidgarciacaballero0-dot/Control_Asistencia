package com.universidad.academico.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad de dominio que representa a un Estudiante universitario.
 */
@Entity
@Table(name = "estudiantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estudiante {

    /**
     * Numero de registro unico del estudiante universitario (Clave Primaria).
     */
    @Id
    @Column(name = "registro", length = 30, nullable = false)
    private String registro;

    @Column(name = "apellidos", length = 100, nullable = false)
    private String apellidos;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "ci", length = 30)
    private String ci;

    @Column(name = "correo", length = 100, nullable = false, unique = true)
    private String correo;

    @Column(name = "carrera", length = 100, nullable = false)
    private String carrera;

    @Column(name = "plan", length = 20, nullable = false)
    private String plan;

    /**
     * Fotografia de perfil del estudiante codificada en formato Base64.
     * Utilizada para la validacion visual de identidad y futuros modulos de reconocimiento facial.
     */
    @Column(name = "foto_base64", columnDefinition = "TEXT")
    private String fotoBase64;
}
