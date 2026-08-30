package com.universidad.asistencia.controller;

import com.universidad.asistencia.dto.IniciarSesionDto;
import com.universidad.asistencia.dto.SesionClaseDto;
import com.universidad.asistencia.service.SesionClaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestion de Sesiones de Clase y generacion de codigos QR.
 */
@RestController
@RequestMapping("/api/v1/asistencia/sesiones")
@RequiredArgsConstructor
public class SesionClaseController {

    private final SesionClaseService sesionService;

    @PostMapping("/iniciar")
    public ResponseEntity<SesionClaseDto> iniciarSesion(@Valid @RequestBody IniciarSesionDto dto) {
        SesionClaseDto sesionIniciada = sesionService.iniciarSesion(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(sesionIniciada);
    }

    @PutMapping("/{sesionId}/finalizar")
    public ResponseEntity<SesionClaseDto> finalizarSesion(@PathVariable Long sesionId) {
        SesionClaseDto sesionFinalizada = sesionService.finalizarSesion(sesionId);
        return ResponseEntity.ok(sesionFinalizada);
    }

    @PutMapping("/{sesionId}/regenerar-qr")
    public ResponseEntity<SesionClaseDto> regenerarCodigoQr(
            @PathVariable Long sesionId,
            @RequestParam(defaultValue = "15") Integer minutosValidez
    ) {
        SesionClaseDto sesionActualizada = sesionService.regenerarCodigoQr(sesionId, minutosValidez);
        return ResponseEntity.ok(sesionActualizada);
    }

    @GetMapping("/{sesionId}")
    public ResponseEntity<SesionClaseDto> obtenerPorId(@PathVariable Long sesionId) {
        return ResponseEntity.ok(sesionService.obtenerPorId(sesionId));
    }

    @GetMapping("/qr/{codigoQr}")
    public ResponseEntity<SesionClaseDto> obtenerPorCodigoQr(@PathVariable String codigoQr) {
        return ResponseEntity.ok(sesionService.obtenerPorCodigoQr(codigoQr));
    }

    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<SesionClaseDto>> listarPorGrupo(@PathVariable Long grupoId) {
        return ResponseEntity.ok(sesionService.listarPorGrupo(grupoId));
    }

    @GetMapping("/docente/{codigoDocente}")
    public ResponseEntity<List<SesionClaseDto>> listarPorDocente(@PathVariable String codigoDocente) {
        return ResponseEntity.ok(sesionService.listarPorDocente(codigoDocente));
    }

    @GetMapping("/activas")
    public ResponseEntity<List<SesionClaseDto>> listarActivas() {
        return ResponseEntity.ok(sesionService.listarActivas());
    }
}
