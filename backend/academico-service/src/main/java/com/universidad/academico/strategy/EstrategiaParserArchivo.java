package com.universidad.academico.strategy;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Interfaz Estrategia_Abs del patron de diseno Strategy (CU03).
 * Sigue estrictamente el diagrama de clases de referencia:
 * Declara la operacion polimorfica +operacion(inputStream) que cada estrategia
 * concreta (Excel, PDF, CSV) implementa para extraer las filas de estudiantes.
 */
public interface EstrategiaParserArchivo {

    /**
     * Ejecuta el algoritmo especifico de lectura y parseo del flujo de entrada del archivo.
     *
     * @param inputStream Flujo binario o de texto del archivo cargado.
     * @return Lista de mapas clave-valor con las cabeceras normalizadas y valores de cada estudiante.
     * @throws Exception Si ocurre un error de formato o lectura de entrada/salida.
     */
    List<Map<String, String>> operacion(InputStream inputStream) throws Exception;

    /**
     * Determina si esta estrategia concreta soporta el archivo segun su nombre o extension.
     *
     * @param nombreArchivo Nombre del archivo en minusculas.
     * @return true si la estrategia procesa este tipo de archivo; false en caso contrario.
     */
    boolean soportaArchivo(String nombreArchivo);
}

