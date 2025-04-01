/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.grupo6.mc1.proyecto1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author usuario
 */
public class LectorArchivosXML {
    
    /**
     * Lee un archivo XML de Karnaugh y devuelve una matriz con sus valores.
     *
     * @param archivoXML Archivo XML con el mapa de Karnaugh.
     * @return Matriz de enteros con los valores del mapa de Karnaugh.
     * @throws Exception Si ocurre un error en la lectura o formato del archivo.
     */
    public int[][] leerMapa(File archivoXML) throws Exception {
        // Lista para almacenar las filas y columnas antes de convertirlas a una matriz
        List<int[]> listaFilas = new ArrayList<>();
        int columnas = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(archivoXML))) {
            String linea;
            
            // Leer cada linea hasta llegar al final del archivo
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();

                // Leer la cantidad de columnas
                if (linea.startsWith("<mapa")) {
                    columnas = extraerAtributo(linea, "columnas");
                }

                // Procesar cada celda
                if (linea.startsWith("<celda")) {
                    int fila = extraerAtributo(linea, "fila");
                    int columna = extraerAtributo(linea, "columna");
                    int valor = extraerAtributo(linea, "valor");

                    // Expandir la lista si la fila aún no ha sido agregada
                    while (listaFilas.size() <= fila) {
                        listaFilas.add(new int[columnas]);
                    }

                    // Insertar el valor en la posición correspondiente
                    listaFilas.get(fila)[columna] = valor;
                }
            }
        }

        // Convertir la lista en una matriz bidimensional
        int filas = listaFilas.size();
        int[][] mapa = new int[filas][columnas];
        for (int i = 0; i < filas; i++) {
            mapa[i] = listaFilas.get(i);
        }

        return mapa;
    }
    
    /**
     * Extrae el valor de un atributo de una línea XML.
     *
     * @param linea Línea XML de la que se extraerá el atributo.
     * @param atributo Nombre del atributo a buscar.
     * @return El valor del atributo como entero.
     * @throws Exception Si el atributo no está presente o tiene un formato incorrecto.
     */
    private int extraerAtributo(String linea, String atributo) throws Exception {
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
    
}
