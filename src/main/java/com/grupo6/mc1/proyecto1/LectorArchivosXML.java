package com.grupo6.mc1.proyecto1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Clase que permite leer un archivo XML simple que representa un mapa de
 * Karnaugh.
 *
 */
public class LectorArchivosXML {

    /**
     * Lee un archivo XML con un mapa de Karnaugh y lo convierte en una matriz
     * 2D.
     *
     * @param archivoXML Archivo que contiene el mapa en formato XML.
     * @return Matriz 2D de enteros representando el mapa de Karnaugh.
     * @throws Exception Si ocurre un error de lectura o formato.
     */
    public MapaKarnaugh leerMapa(File archivoXML) throws Exception {
        int cantVariables = 0;
        int[] valores = new int[0];
        String[] nombresVariables = new String[8]; // máximo 8 variables

        try (BufferedReader br = new BufferedReader(new FileReader(archivoXML))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();

                // Detectar inicio del mapa
                if (linea.startsWith("<mapa")) {
                    // Extrar la cantidad de variables (ej. v="2")
                    cantVariables = extraerAtributoNumero(linea, "v");
                    int totalCeldas = (int) Math.pow(2, cantVariables);
                    valores = new int[totalCeldas];

                    // Extrar nombres de variables (ej. val1="X")
                    for (int i = 1; i <= cantVariables; i++) {
                        String nombre = extraerAtributoTexto(linea, "val" + i);
                        nombresVariables[i - 1] = nombre;
                    }
                }

                // Extraer el valor de cada celda
                if (linea.startsWith("<valor")) {
                    int min = extraerAtributoNumero(linea, "min");
                    int valor = extraerContenidoValor(linea);
                    valores[min] = valor;
                }
            }
        }

        // Determinar dimensiones del mapa
        int filas = (int) Math.pow(2, cantVariables / 2);
        int columnas = valores.length / filas;
        
        // Crear el mapa
        int[][] mapa = new int[filas][columnas];
        for (int i = 0; i < valores.length; i++) {
            int fila = i / columnas;
            int columna = i % columnas;
            mapa[fila][columna] = valores[i];
        }

        // Retornar instancia del Objeto MapaKarnaugh
        return new MapaKarnaugh(mapa, nombresVariables);
    }

    /**
     * 
     * @param linea Línea con formato XML
     * @param atributo Nombre del atributo a buscar
     * @return Valor del atributo como una cadena de texto
     * @throws Exception Si no se encuentra el atributo o está mal formado.
     */
    private String extraerAtributoTexto(String linea, String atributo) throws Exception {
        String patron = atributo + "=\"";
        int inicio = linea.indexOf(patron);
        if (inicio == -1) {
            throw new Exception("Atributo '" + atributo + "' no encontrado en la línea: " + linea);
        }
        inicio += patron.length();
        int fin = linea.indexOf("\"", inicio);
        if (fin == -1) {
            throw new Exception("Error al leer el atributo '" + atributo + "' en la línea: " + linea);
        }
        return linea.substring(inicio, fin);
    }

    /**
     * Extrae un atributo entero desde una línea XML.
     *
     * @param linea Línea con formato XML.
     * @param atributo Nombre del atributo a buscar.
     * @return Valor del atributo como entero.
     * @throws Exception Si no se encuentra el atributo o está mal formado.
     */
    private int extraerAtributoNumero(String linea, String atributo) throws Exception {
        String patron = atributo + "=\"";
        int inicio = linea.indexOf(patron);
        if (inicio == -1) {
            throw new Exception("Atributo '" + atributo + "' no encontrado en la línea: " + linea);
        }
        inicio += patron.length();
        int fin = linea.indexOf("\"", inicio);
        if (fin == -1) {
            throw new Exception("Error al leer el atributo '" + atributo + "' en la línea: " + linea);
        }
        return Integer.parseInt(linea.substring(inicio, fin));
    }

    /**
     * Extrae el contenido (valor) entre etiquetas <valor>...</valor>.
     *
     * @param linea Línea donde empieza la apertura de la etiqueta <valor>.
     * @return El valor entero contenido en la línea.
     * @throws Exception Si no puede extraer el contenido.
     */
    private int extraerContenidoValor(String linea) throws Exception {
        int inicio = linea.indexOf(">") + 1;
        int fin = linea.indexOf("</valor>");
        if (inicio == 0 || fin == -1) {
            throw new Exception("Contenido de <valor> mal formado: " + linea);
        }
        return Integer.parseInt(linea.substring(inicio, fin).trim());
    }
}
