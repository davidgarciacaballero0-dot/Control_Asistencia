package com.universidad.academico.strategy.impl;

import com.universidad.academico.strategy.EstrategiaParserArchivo;
import com.universidad.academico.strategy.util.NormalizadorCabecerasUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Estrategia concreta Concretec_A del patron Strategy (CU03).
 * Implementa la lectura de libros Excel (.xlsx y .xls) utilizando Apache POI.
 */
@Component
@Order(1)
public class ExcelParserConcreteStrategy implements EstrategiaParserArchivo {

    @Override
    public boolean soportaArchivo(String nombreArchivo) {
        if (nombreArchivo == null) {
            return false;
        }
        String lower = nombreArchivo.trim().toLowerCase();
        return lower.endsWith(".xlsx") || lower.endsWith(".xls");
    }

    @Override
    public List<Map<String, String>> operacion(InputStream inputStream) throws Exception {
        List<Map<String, String>> filas = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                return filas;
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return filas;
            }

            List<String> cabecerasList = new ArrayList<>();
            for (Cell cell : headerRow) {
                cabecerasList.add(formatter.formatCellValue(cell).trim());
            }

            String[] cabeceras = NormalizadorCabecerasUtil.normalizarCabeceras(
                    cabecerasList.toArray(new String[0])
            );

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }

                Map<String, String> fila = new HashMap<>();
                boolean tieneContenido = false;

                for (int c = 0; c < cabeceras.length; c++) {
                    Cell cell = row.getCell(c);
                    String valor = (cell != null) ? formatter.formatCellValue(cell).trim() : "";
                    if (!valor.isEmpty()) {
                        tieneContenido = true;
                    }
                    fila.put(cabeceras[c], valor);
                }

                if (tieneContenido) {
                    filas.add(fila);
                }
            }
        }
        return filas;
    }
}

