package com.universidad.academico.strategy.util;

/**
 * Utilidad de soporte para normalizar nombres de cabeceras y limpiar valores de celdas
 * en las diferentes estrategias concretas de parseo tabular (Excel y CSV).
 */
public final class NormalizadorCabecerasUtil {

    private NormalizadorCabecerasUtil() {
        // Clase utilitaria no instanciable
    }

    /**
     * Normaliza un arreglo de cabeceras convirtiendolas a minusculas, sin tildes
     * y reemplazando espacios por guiones bajos.
     *
     * @param raw Arreglo de cabeceras originales.
     * @return Arreglo de cabeceras normalizadas.
     */
    public static String[] normalizarCabeceras(String[] raw) {
        String[] normalizadas = new String[raw.length];
        for (int i = 0; i < raw.length; i++) {
            String col = raw[i].trim().toLowerCase()
                    .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                    .replace("ñ", "n").replace(" ", "_").replaceAll("[^a-z0-9_]", "");
            normalizadas[i] = col;
        }
        return normalizadas;
    }

    /**
     * Remueve comillas dobles circundantes y espacios en blanco de un valor de celda.
     *
     * @param val Valor original de la celda.
     * @return Valor limpio sin comillas externas.
     */
    public static String limpiarValor(String val) {
        if (val == null) {
            return "";
        }
        String s = val.trim();
        while (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1).trim();
        }
        return s;
    }
}

