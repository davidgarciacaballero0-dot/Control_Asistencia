package com.universidad.academico.strategy;

import com.universidad.academico.strategy.impl.CsvParserConcreteStrategy;
import com.universidad.academico.strategy.impl.ExcelParserConcreteStrategy;
import com.universidad.academico.strategy.impl.PdfParserConcreteStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Clase Contexto del patron de diseno Strategy (CU03).
 * Sigue estrictamente el diagrama de clases de referencia en Enterprise Architect:
 * - Mantiene el atributo privado -estrategia : EstrategiaParserArchivo.
 * - Expone el metodo +setEstrategia(EstrategiaParserArchivo) : void.
 * - Expone el metodo +ejecutarOperacion(InputStream) : List<Map<String, String>>.
 */
@Component
public class ContextoParserArchivo {

    /**
     * Referencia privada a la interfaz Estrategia_Abs segun el diagrama de referencia.
     */
    private EstrategiaParserArchivo estrategia;

    /**
     * Catalogo de estrategias concretas inyectadas por Spring para seleccion sin condicionales if/else.
     */
    private final List<EstrategiaParserArchivo> estrategiasRegistradas;

    @Autowired
    public ContextoParserArchivo(List<EstrategiaParserArchivo> estrategiasRegistradas) {
        this.estrategiasRegistradas = estrategiasRegistradas;
    }

    /**
     * Constructor por defecto para pruebas unitarias directas fuera del contenedor de Spring.
     */
    public ContextoParserArchivo() {
        this(List.of(
                new ExcelParserConcreteStrategy(),
                new PdfParserConcreteStrategy(),
                new CsvParserConcreteStrategy()
        ));
    }

    /**
     * Asigna dinamicamente la estrategia concreta que el contexto ejecutara.
     *
     * @param estrategia Implementacion concreta de EstrategiaParserArchivo.
     */
    public void setEstrategia(EstrategiaParserArchivo estrategia) {
        this.estrategia = estrategia;
    }

    /**
     * Obtiene la estrategia actualmente asignada en el contexto.
     *
     * @return Estrategia activa.
     */
    public EstrategiaParserArchivo getEstrategia() {
        return this.estrategia;
    }

    /**
     * Delega la ejecucion del parseo a la estrategia activa mediante su metodo polimorfico operacion().
     *
     * @param inputStream Flujo de datos del archivo a procesar.
     * @return Lista de filas tabulares extraidas por la estrategia concreta.
     * @throws Exception Si no hay estrategia configurada o falla la lectura del archivo.
     */
    public List<Map<String, String>> ejecutarOperacion(InputStream inputStream) throws Exception {
        if (this.estrategia == null) {
            throw new IllegalStateException("No se ha asignado una estrategia de parseo en ContextoParserArchivo");
        }
        return this.estrategia.operacion(inputStream);
    }

    /**
     * Resuelve y asigna mediante setEstrategia la implementacion compatible con el nombre del archivo,
     * eliminando por completo las estructuras condicionales if / else if / else en el servicio cliente.
     *
     * @param nombreArchivo Nombre original del archivo cargado.
     */
    public void seleccionarEstrategiaPorArchivo(String nombreArchivo) {
        String nombreNormalizado = (nombreArchivo != null) ? nombreArchivo.trim().toLowerCase() : "";
        EstrategiaParserArchivo seleccionada = estrategiasRegistradas.stream()
                .filter(item -> item.soportaArchivo(nombreNormalizado))
                .findFirst()
                .orElseGet(CsvParserConcreteStrategy::new);
        this.setEstrategia(seleccionada);
    }
}

