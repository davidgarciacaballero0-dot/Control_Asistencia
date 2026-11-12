package com.universidad.academico.strategy.impl;

import com.universidad.academico.strategy.EstrategiaParserArchivo;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Estrategia concreta Concretec_B del patron Strategy (CU03).
 * Implementa la extraccion de filas tabulares de estudiantes desde archivos PDF oficiales
 * utilizando Apache PDFBox.
 */
@Component
@Order(2)
public class PdfParserConcreteStrategy implements EstrategiaParserArchivo {

    @Override
    public boolean soportaArchivo(String nombreArchivo) {
        if (nombreArchivo == null) {
            return false;
        }
        return nombreArchivo.trim().toLowerCase().endsWith(".pdf");
    }

    @Override
    public List<Map<String, String>> operacion(InputStream inputStream) throws Exception {
        List<Map<String, String>> filas = new ArrayList<>();
        byte[] bytes = inputStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            String[] lineas = text.split("\\r?\\n");
            for (String linea : lineas) {
                linea = linea.trim();
                // Patron tipico de padron universitario: Nro Registro Apellidos_y_Nombres Carr Plan Tel/Cel Email
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
                            if (!nombreCompleto.isEmpty()) {
                                nombreCompleto.append(" ");
                            }
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
}

