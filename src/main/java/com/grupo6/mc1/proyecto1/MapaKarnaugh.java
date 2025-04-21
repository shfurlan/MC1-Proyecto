/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.grupo6.mc1.proyecto1;

import java.util.ArrayList;

/**
 * Clase que representa un mapa de Karnaugh. Permite generar su forma disyuntiva
 * canónica y una expresión simplificada, además de detectar burbujas que
 * representan agrupaciones lógicas.
 */
public class MapaKarnaugh {

    public int[][] mapa;
    public String[] variables;
    public String[] filasGray;
    public String[] columnasGray;
    public int varFilas;
    public int varColumnas;

    /**
     * Constructor del mapa de Karnaugh.
     *
     * @param mapa Matriz de enteros que representa el contenido lógico del mapa
     * (0s y 1s).
     * @param variables Arreglo de nombres de variables en orden.
     */
    public MapaKarnaugh(int[][] mapa, String[] variables) {
        this.mapa = mapa;
        this.variables = variables;
        this.filasGray = generarGray(mapa.length);
        this.columnasGray = generarGray(mapa[0].length);
        this.varFilas = (int) (Math.log(mapa.length) / Math.log(2));
        this.varColumnas = (int) (Math.log(mapa[0].length) / Math.log(2));
    }

    /**
     * Genera los códigos Gray para n valores consecutivos.
     *
     * @param n Cantidad de combinaciones (debe ser potencia de 2).
     * @return Arreglo de strings con los códigos Gray.
     */
    private String[] generarGray(int n) {
        int bits = (int) (Math.log(n) / Math.log(2));
        String[] gray = new String[n];
        for (int i = 0; i < n; i++) {
            int g = i ^ (i >> 1);
            gray[i] = String.format("%" + bits + "s", Integer.toBinaryString(g)).replace(' ', '0');
        }
        return gray;
    }

    /**
     * Genera la forma disyuntiva canónica de la función booleana del mapa.
     *
     * @return Cadena de texto con la suma de minitérminos.
     */
    public String generarFormaDisyuntivaCanonica() {
        var terminos = new ArrayList<String>();

        // Recorrer el mapa
        for (int i = 0; i < varFilas; i++) {
            for (int j = 0; j < varColumnas; j++) {

                // Al encontrar un uno agregar su minitérmino a la cadena
                if (mapa[i][j] == 1) {
                    StringBuilder sb = new StringBuilder();
                    for (int k = 0; k < varFilas; k++) {
                        sb.append(variables[k]);
                        if (filasGray[i].charAt(k) == '0') {
                            sb.append("'");
                        }
                    }
                    for (int k = 0; k < columnasGray[j].length(); k++) {
                        sb.append(variables[varFilas + k]);
                        if (columnasGray[j].charAt(k) == '0') {
                            sb.append("'");
                        }
                    }
                    terminos.add(sb.toString());
                }
            }
        }
        return String.join(" + ", terminos);
    }

    /**
     * Genera una expresión simplificada en forma de suma de productos a partir
     * de burbujas.
     *
     * @param listaBurbujas Lista de burbujas detectadas previamente.
     * @return Cadena con la expresión booleana simplificada.
     */
    public String generarExpresionSimplificada(ArrayList<int[][]> listaBurbujas) {
        ArrayList<int[][]> burbujas = filtrarBurbujasMaximas(listaBurbujas, mapa.length, mapa[0].length);
        ArrayList<String> terminos = new ArrayList<>();

        for (int[][] burbuja : burbujas) {
            // Obtener todas las celdas de la burbuja
            ArrayList<int[]> celdas = obtenerCeldas(burbuja, mapa.length, mapa[0].length);
            StringBuilder sb = new StringBuilder();

            // Evaluar variable por variable si en todas las celdas es uno o es 0
            for (int varIndice = 0; varIndice < variables.length; varIndice++) {
                boolean todas1 = true;
                boolean todas0 = true;
                
                // Recorrer cada celda para verificar el bit al que corresponde la variabel
                for (int[] celda : celdas) {
                    int fila = celda[0];
                    int columna = celda[1];
                    char bitCompartido;
                    
                    // Si la variable a evaluar corresponde a una fila
                    if (varIndice < varFilas) {
                        bitCompartido = filasGray[fila].charAt(varIndice);
                    } else {
                        int bitIndice = varIndice - filasGray[fila].length();
                        if (bitIndice < columnasGray[columna].length()) {
                            bitCompartido = columnasGray[columna].charAt(bitIndice);
                        } else {
                            todas1 = todas0 = false;
                            break;
                        }
                    }
                    
                    // Actualizar las variables temporales
                    if (bitCompartido == '1') {
                        todas0 = false;
                    } else {
                        todas1 = false;
                    }
                    
                    // Si difiere en sus bits en todas entonces terminar bucle de una burbuja
                    if (!todas1 && !todas0) {
                        break;
                    }
                }
                
                // Añadir celda a la lista de términos
                if (todas1) {
                    sb.append(variables[varIndice]);
                } else if (todas0) {
                    sb.append(variables[varIndice]).append("'");
                }
            }
            terminos.add(sb.toString());
        }
        return String.join(" + ", terminos);
    }

    /**
     * Busca la burbuja rectangular más grande de área potencia de 2 que puede
     * crearse desde una celda con valor 1.
     *
     * @param mapa Matriz del mapa de Karnaugh.
     * @param posicion Posición inicial en formato {fila, columna}.
     * @return Coordenadas de la burbuja más grande encontrada.
     */
    private static int[][] construirBurbujas(int[][] mapa, int[] posicion) {
        int filasTotal = mapa.length;
        int columnasTotal = mapa[0].length;
        int filaInicio = posicion[0];
        int colInicio = posicion[1];
        int mejorAlto = 1;
        int mejorAncho = 1;

        // Probamos todos los tamaños potencia de dos posibles
        for (int alto = 1; alto <= filasTotal; alto *= 2) {
            for (int ancho = 1; ancho <= columnasTotal; ancho *= 2) {
                // Asumir que todas las celdas contienen un uno
                boolean todosUnos = true;
                
                // Verificamos cada celda del rectángulo
                for (int f = 0; f < alto && todosUnos; f++) {
                    for (int c = 0; c < ancho; c++) {
                        int fila = (filaInicio + f) % filasTotal;
                        int col = (colInicio + c) % columnasTotal;
                        if (mapa[fila][col] != 1) {
                            todosUnos = false;
                            break;
                        }
                    }
                }
                // Si está lleno de unos y el área es mayor, se guarda la burbuja
                if (todosUnos && alto * ancho > mejorAlto * mejorAncho) {
                    mejorAlto = alto;
                    mejorAncho = ancho;
                }
            }
        }

        // Calcular la esquina opuesta de la burbuja porque hay borde compartido en el mapa
        int filaFinal = (filaInicio + mejorAlto - 1) % filasTotal;
        int colFinal = (colInicio + mejorAncho - 1) % columnasTotal;
        return new int[][]{
            {filaInicio, colInicio},
            {filaFinal, colFinal}
        };
    }

    /**
     * Recorre todo el mapa y, para cada 1, construye su burbuja más grande.
     *
     * @param mapa matriz de Karnaugh
     * @return lista de burbujas; cada burbuja es {{filaIni, colIni}, {filaFin,
     * colFin}}
     */
    public static ArrayList<int[][]> obtenerBurbujas(int[][] mapa) {
        ArrayList<int[][]> burbujas = new ArrayList<>();
        for (int i = 0; i < mapa.length; i++) {
            for (int j = 0; j < mapa[0].length; j++) {
                if (mapa[i][j] == 1) {
                    burbujas.add(construirBurbujas(mapa, new int[]{i, j}));
                }
            }
        }
        return filtrarBurbujasMaximas(burbujas, mapa.length, mapa[0].length);
    }

    /**
     * Filtra las burbujas para quedarse sólo con las de mayor área, eliminando
     * duplicados y las que quedan contenidas en otras más grandes.
     *
     * @param burbujas Lista de {{filaIni,colIni},{filaFin,colFin}}
     * @param filas Número de filas del mapa (mapa.length)
     * @param columnas Número de columnas del mapa (mapa[0].length)
     * @return lista filtrada de burbujas máximas
     */
    public static ArrayList<int[][]> filtrarBurbujasMaximas(
            ArrayList<int[][]> burbujas,
            int filas,
            int columnas
    ) {
        int n = burbujas.size();
        ArrayList<Integer> indices = new ArrayList<>(n);
        ArrayList<Integer> areas = new ArrayList<>(n);

        // Calcular área de cada burbuja
        for (int i = 0; i < n; i++) {
            int[][] b = burbujas.get(i);
            int filaInicio = b[0][0], colInicio = b[0][1];
            int filaFinal = b[1][0], colFinal = b[1][1];
            
            int alto = (filaFinal >= filaInicio) ? 
                    filaFinal - filaInicio + 1 : 
                    (filas - filaInicio) + filaFinal + 1;
            int ancho = (colFinal >= colInicio) ? 
                    colFinal - colInicio + 1 : 
                    (columnas - colInicio) + colFinal + 1;
            
            // Agregar información de burbuja
            indices.add(i);
            areas.add(alto * ancho);
        }

        // Ordenar índices de mayor a menor área (método de ordentamiento burbuja :D)
        for (int i = 0; i < indices.size(); i++) {
            for (int j = i + 1; j < indices.size(); j++) {
                if (areas.get(indices.get(j)) > areas.get(indices.get(i))) {
                    int tmp = indices.get(i);
                    indices.set(i, indices.get(j));
                    indices.set(j, tmp);
                }
            }
        }

        // Eliminar burbujas contenidas en otras más grandes
        ArrayList<int[][]> resultado = new ArrayList<>();
        ArrayList<ArrayList<int[]>> listas = new ArrayList<>();

        for (int i : indices) {
            int[][] b = burbujas.get(i);
            ArrayList<int[]> celdas = obtenerCeldas(b, filas, columnas);
            
            boolean burbujaContenida = false;
            
            // Verificar si la burbuja está contenida en otra más grande
            for (ArrayList<int[]> otras : listas) {
                if (contieneTodas(otras, celdas)) {
                    burbujaContenida = true;
                    break;
                }
            }
            
            if (!burbujaContenida) {
                resultado.add(b);
                listas.add(celdas);
            }
        }

        // Eliminar burbujas cuyas celdas estén totalmente compartidas
        ArrayList<int[][]> depuradas = new ArrayList<>();
        for (int i = 0; i < resultado.size(); i++) {
            int[][] bAct = resultado.get(i);
            ArrayList<int[]> celdasBurbuja = obtenerCeldas(bAct, filas, columnas);

            // Recolectar todas las celdas de las otras burbujas
            ArrayList<int[]> celdasOtras = new ArrayList<>();
            for (int j = 0; j < resultado.size(); j++) {
                if (j == i) {
                    continue;
                }
                celdasOtras.addAll(obtenerCeldas(resultado.get(j), filas, columnas));
            }

            // Si NO está totalmente compartida, mantener celda
            if (!contieneTodas(celdasOtras, celdasBurbuja)) {
                depuradas.add(bAct);
            }
        }

        return depuradas;
    }

    /**
     * Genera la lista de celdas cubiertas por un rectángulo en un mapa con
     * borde conectado (mapa toroidal). Toma como entrada las coordenadas de
     * inicio y fin del rectángulo, calcula su altura y ancho teniendo en cuenta
     * que puede “voltear” sobre los bordes, y devuelve cada posición como un
     * par [fila, columna].
     *
     * @param burbuja matriz 2×2 con las celdas de inicio y final de la burbuja
     * @param filas número total de filas del mapa (mapa.length)
     * @param columnas número total de columnas del mapa (mapa[0].length)
     * @return ArrayList de int[2], cada elemento es {fila, columna} de una
     * celda cubierta por el rectángulo
     */
    private static ArrayList<int[]> obtenerCeldas(
            int[][] burbuja,
            int filas,
            int columnas
    ) {
        int filaInicio = burbuja[0][0], colInicio = burbuja[0][1];
        int filaFin = burbuja[1][0], colFinal = burbuja[1][1];
        
        // Calcular dimensiones de brubuja
        int alto = (filaFin >= filaInicio) ? 
                filaFin - filaInicio + 1 : 
                (filas - filaInicio) + filaFin + 1;
        int ancho = (colFinal >= colInicio) ? 
                colFinal - colInicio + 1 : 
                (columnas - colInicio) + colFinal + 1;

        // Recorrer burbuja agregando cada celda al conjunto de celdas
        ArrayList<int[]> lista = new ArrayList<>(alto * ancho);
        for (int f = 0; f < alto; f++) {
            for (int c = 0; c < ancho; c++) {
                int fila = (filaInicio + f) % filas;
                int columna = (colInicio + c) % columnas;
                lista.add(new int[]{fila, columna});
            }
        }
        return lista;
    }

    
    
    /**
     * Comprueba si una lista de celdas (listaMayor) contiene todas las celdas
     * de otra lista (listaMenor), comparando manualmente cada par [fila,
     * columna].
     *
     * @param listaMayor ArrayList de int[2], lista en la que buscar
     * @param listaMenor ArrayList de int[2], lista cuyas celdas deben estar
     * todas en listaMayor
     * @return true si cada {fila, columna} de listaMenor aparece en listaMayor;
     * false en caso contrario
     */
    private static boolean contieneTodas(
            ArrayList<int[]> listaMayor,
            ArrayList<int[]> listaMenor
    ) {
        outer:
        for (int[] celda : listaMenor) {
            for (int[] otraCelda : listaMayor) {
                if (celda[0] == otraCelda[0] && celda[1] == otraCelda[1]) {
                    continue outer;
                }
            }
            return false;
        }
        return true;
    }

}
