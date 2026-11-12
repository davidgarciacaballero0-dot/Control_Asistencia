package com.universidad.academico.strategy;

import com.universidad.academico.strategy.impl.CsvParserConcreteStrategy;
import com.universidad.academico.strategy.impl.ExcelParserConcreteStrategy;
import com.universidad.academico.strategy.impl.PdfParserConcreteStrategy;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias del patron Strategy (CU03: Gestionar Estudiante).
 * Verifica la clase ContextoParserArchivo (setEstrategia, ejecutarOperacion)
 * y las estrategias concretas ExcelParserConcreteStrategy, PdfParserConcreteStrategy y CsvParserConcreteStrategy.
 */
class ContextoParserArchivoStrategyTest {

    private ContextoParserArchivo contexto;

    @BeforeEach
    void setUp() {
        contexto = new ContextoParserArchivo(List.of(
                new ExcelParserConcreteStrategy(),
                new PdfParserConcreteStrategy(),
                new CsvParserConcreteStrategy()
        ));
    }

    @Test
    @DisplayName("Debe seleccionar dinamicamente ExcelParserConcreteStrategy para archivos .xlsx y .xls")
    void seleccionarEstrategiaPorArchivo_FormatoExcel_AsignaExcelStrategy() {
        contexto.seleccionarEstrategiaPorArchivo("lista_estudiantes.xlsx");
        assertInstanceOf(ExcelParserConcreteStrategy.class, contexto.getEstrategia());

        contexto.seleccionarEstrategiaPorArchivo("planilla_antigua.xls");
        assertInstanceOf(ExcelParserConcreteStrategy.class, contexto.getEstrategia());
    }

    @Test
    @DisplayName("Debe seleccionar dinamicamente PdfParserConcreteStrategy para archivos .pdf")
    void seleccionarEstrategiaPorArchivo_FormatoPdf_AsignaPdfStrategy() {
        contexto.seleccionarEstrategiaPorArchivo("padron_uagrm.pdf");
        assertInstanceOf(PdfParserConcreteStrategy.class, contexto.getEstrategia());
    }

    @Test
    @DisplayName("Debe seleccionar dinamicamente CsvParserConcreteStrategy para archivos .csv")
    void seleccionarEstrategiaPorArchivo_FormatoCsv_AsignaCsvStrategy() {
        contexto.seleccionarEstrategiaPorArchivo("estudiantes.csv");
        assertInstanceOf(CsvParserConcreteStrategy.class, contexto.getEstrategia());
    }

    @Test
    @DisplayName("Debe parsear correctamente un flujo CSV mediante setEstrategia y ejecutarOperacion")
    void ejecutarOperacion_ConCsvStrategy_RetornaFilasNormalizadas() throws Exception {
        String contenidoCsv = "Registro;Apellidos;Nombre;Carrera;Plan;Correo\n" +
                              "217058795;Garcia Caballero;David;Ingenieria Informatica;2020;david@uagrm.edu\n" +
                              "2024001;Perez Gomez;Juan;Ingenieria Informatica;2020;juan@uagrm.edu\n";

        contexto.setEstrategia(new CsvParserConcreteStrategy());
        List<Map<String, String>> filas = contexto.ejecutarOperacion(
                new ByteArrayInputStream(contenidoCsv.getBytes(StandardCharsets.UTF_8))
        );

        assertEquals(2, filas.size());
        assertEquals("217058795", filas.get(0).get("registro"));
        assertEquals("Garcia Caballero", filas.get(0).get("apellidos"));
        assertEquals("David", filas.get(0).get("nombre"));
    }

    @Test
    @DisplayName("Debe parsear correctamente un libro Excel .xlsx en memoria usando ExcelParserConcreteStrategy")
    void ejecutarOperacion_ConExcelStrategy_RetornaFilasNormalizadas() throws Exception {
        byte[] bytesExcel;
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Estudiantes");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Registro");
            header.createCell(1).setCellValue("Apellidos");
            header.createCell(2).setCellValue("Nombre");

            Row fila1 = sheet.createRow(1);
            fila1.createCell(0).setCellValue("2024002");
            fila1.createCell(1).setCellValue("Garcia Lopez");
            fila1.createCell(2).setCellValue("Maria");

            wb.write(baos);
            bytesExcel = baos.toByteArray();
        }

        contexto.setEstrategia(new ExcelParserConcreteStrategy());
        List<Map<String, String>> filas = contexto.ejecutarOperacion(new ByteArrayInputStream(bytesExcel));

        assertEquals(1, filas.size());
        assertEquals("2024002", filas.get(0).get("registro"));
        assertEquals("Garcia Lopez", filas.get(0).get("apellidos"));
        assertEquals("Maria", filas.get(0).get("nombre"));
    }
}

