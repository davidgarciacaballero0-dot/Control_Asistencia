package com.universidad.academico.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PdfExtractorTest {

    @Test
    public void testExtraerImagenesPdf() throws Exception {
        File pdfFile = new File("d:/HDD/DOCS/docs/Universidad/ARQUITECTURA DE SOFTWARE/1ER EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/DATOS DE PRUEBAS Y FORMATO/UAGRM  Perfil Lista de estudiantes.pdf");
        if (!pdfFile.exists()) {
            System.out.println("Archivo PDF no encontrado para prueba local, se omite.");
            return;
        }

        List<String> nombresImagenes = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            assertNotNull(document);
            int totalPaginas = document.getNumberOfPages();
            System.out.println("Total paginas en PDF: " + totalPaginas);

            for (int p = 0; p < totalPaginas; p++) {
                PDPage page = document.getPage(p);
                PDResources resources = page.getResources();
                if (resources == null) continue;

                for (COSName xObjectName : resources.getXObjectNames()) {
                    PDXObject xObject = resources.getXObject(xObjectName);
                    if (xObject instanceof PDImageXObject image) {
                        nombresImagenes.add("Pagina " + (p + 1) + " - " + xObjectName.getName() + " (" + image.getWidth() + "x" + image.getHeight() + ")");
                    }
                }
            }
        }
        System.out.println("Total imagenes encontradas: " + nombresImagenes.size());
        for (int i = 0; i < Math.min(10, nombresImagenes.size()); i++) {
            System.out.println("Img " + (i + 1) + ": " + nombresImagenes.get(i));
        }
    }
}
