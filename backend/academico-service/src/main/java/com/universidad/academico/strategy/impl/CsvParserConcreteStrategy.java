package com.universidad.academico.strategy.impl;

import com.universidad.academico.strategy.EstrategiaParserArchivo;
import com.universidad.academico.strategy.util.NormalizadorCabecerasUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Estrategia concreta Concretec_N del patron Strategy (CU03).
 * Implementa el parseo de archivos CSV delimitados por coma, punto y coma o tabulacion,
 * soportando marca de orden de bytes (BOM UTF-8) y escapado de comillas de Excel.
 */
@Component
@Order(3)
public class CsvParserConcreteStrategy implements EstrategiaParserArchivo {

    @Override
    public boolean soportaArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            return true;
        }
        String lower = nombreArchivo.trim().toLowerCase();
        return lower.endsWith(".csv") || lower.endsWith(".txt");
    }

    @Override
    public List<Map<String, String>> operacion(InputStream inputStream) throws Exception {
        List<Map<String, String>> filas = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String lineaCabecera = reader.readLine();
            if (lineaCabecera == null) {
                return filas;
            }

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
            String[] cabeceras = NormalizadorCabecerasUtil.normalizarCabeceras(
                    cabecerasRaw.toArray(new String[0])
            );

            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                linea = desescaparLineaCsvSiEsNecesario(linea);
                List<String> valores = dividirLineaCsv(linea, delimitador);
                Map<String, String> fila = new HashMap<>();
                for (int i = 0; i < cabeceras.length && i < valores.size(); i++) {
                    fila.put(cabeceras[i], NormalizadorCabecerasUtil.limpiarValor(valores.get(i)));
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
        if (linea == null) {
            return "";
        }
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
}

