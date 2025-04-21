package com.grupo6.mc1.proyecto1.InterfazGrafica;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 * Definir panel personalizado capaz de pintar burbujas sobre un mapa de
 * Karnaugh.
 */
public class PanelMapaConBurbujas extends JPanel {

    /**
     * Lista de burbujas representadas como rectángulos (coordenadas inicio y
     * fin).
     */
    private ArrayList<int[][]> listaBurbujas;

    /**
     * Inicializar panel con layout especificado.
     *
     * @param layout Gestor de disposición de componentes (GridBagLayout).
     */
    public PanelMapaConBurbujas(LayoutManager layout) {
        super(layout);
    }

    /**
     * Asignar nueva lista de burbujas y solicitar repintado del panel.
     *
     * @param burbujas Rectángulos de burbujas
     * {{filaIni,colIni},{filaFin,colFin}}.
     */
    public void setBurbujas(ArrayList<int[][]> burbujas) {
        this.listaBurbujas = burbujas;
        repaint();
    }

    /**
     * Pintar componentes base y superponer las burbujas coloreadas.
     *
     * @param g Contexto gráfico proporcionado por Swing.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (listaBurbujas == null) {
            return;
        }

        // Crear contexto 2D y establecer grosor de trazo
        Graphics2D lienzo2D = (Graphics2D) g.create();
        lienzo2D.setStroke(new BasicStroke(3));

        // Calcular área que abarca solo las celdas de datos (excluir encabezados)
        Rectangle areaDatos = calcularAreaCeldas();
        int padding = 6;

        // Tomar una referencia de celda para obtener dimensiones
        Component referenciaCelda = encontrarCelda(1, 1);
        int anchoCelda = referenciaCelda.getBounds().width;
        int altoCelda = referenciaCelda.getBounds().height;

        // Coordenadas del area de que cubre las celdas
        int gridXIni = areaDatos.x;
        int gridYIni = areaDatos.y;
        int gridXFin = areaDatos.x + areaDatos.width;
        int gridYFin = areaDatos.y + areaDatos.height;

        // Dibujar cada burbuja según sus coordenadas
        for (int indiceBurbuja = 0; indiceBurbuja < listaBurbujas.size(); indiceBurbuja++) {
            int[][] coordenadas = listaBurbujas.get(indiceBurbuja);

            
            int[] inicio = coordenadas[0];
            int[] fin = coordenadas[1];
            Component celdaInicio = encontrarCelda(inicio[0] + 1, inicio[1] + 1);
            Component celdaFin = encontrarCelda(fin[0] + 1, fin[1] + 1);
            if (celdaInicio == null || celdaFin == null) {
                continue;
            }

            Rectangle rectCeldaInicio = celdaInicio.getBounds();
            Rectangle rectCeldaFin = celdaFin.getBounds();

            boolean desbordeHorizontal = fin[1] < inicio[1];
            boolean desbordeVertical = fin[0] < inicio[0];

            // Calcular coordenadas de inicio de burbuja
            int burbXIni = Math.max(gridXIni - padding, rectCeldaInicio.x - padding);
            int burbYIni = Math.max(gridYIni - padding, rectCeldaInicio.y - padding);
           
            // Calcular coordenadas de final de burbuja
            int burbXFin = desbordeHorizontal ? 
                    gridXFin + padding : 
                    rectCeldaFin.x + anchoCelda + padding;
            int burbYFin = desbordeVertical ? 
                    gridYFin + padding : 
                    rectCeldaFin.y + altoCelda + padding;
            burbXFin = Math.min(gridXFin + padding, burbXFin);
            burbYFin = Math.min(gridYFin + padding, burbYFin);

            // Calcular dimension de burbuja
            int anchoPrincipal = burbXFin - burbXIni;
            int altoPrincipal  = burbYFin - burbYIni;

            // Colores de burbuja
            Color colorRelleno = new Color(PaletaColores.coloresBurbujas[indiceBurbuja], true);
            Color colorBorde = new Color(PaletaColores.coloresBordes[indiceBurbuja], true);

            // Dibujar burbuja no desbordada
            dibujarRectangulo(lienzo2D, burbXIni, burbYIni, anchoPrincipal, altoPrincipal, colorRelleno, colorBorde);

            // Dibujar burbuja que de sale del mapa a lo horizontal
            if (desbordeHorizontal) {
                int desbordeXIni = gridXIni - padding;
                int desbordeXFin = Math.min(gridXFin + padding, rectCeldaFin.x + anchoCelda + padding);
                dibujarRectangulo(lienzo2D, desbordeXIni, burbYIni, desbordeXFin - desbordeXIni, altoPrincipal, colorRelleno, colorBorde);
            }

            // Dibujar burbuja que de sale del mapa a lo vertical
            if (desbordeVertical) {
                int desbordeYIni = gridYIni - padding;
                int desbordeYFin = Math.min(gridYFin + padding, rectCeldaFin.y + altoCelda + padding);
                dibujarRectangulo(lienzo2D, burbXIni, desbordeYIni, anchoPrincipal, desbordeYFin - desbordeYIni, colorRelleno, colorBorde);
            }

            // Dibujar burbuja que se sale del mapa en ambas direcciones (Combinación de las dos anteriores)
            if (desbordeHorizontal && desbordeVertical) {
                int desbordeXIni = gridXIni - padding;
                int desbordeYIni = gridYIni - padding;
                int desbordeXFin = Math.min(gridXFin + padding, rectCeldaFin.x + anchoCelda + padding);
                int desbordeYFin = Math.min(gridYFin + padding, rectCeldaFin.y + altoCelda + padding);
                dibujarRectangulo(lienzo2D, desbordeXIni, desbordeYIni, desbordeXFin - desbordeXIni, desbordeYFin - desbordeYIni, colorRelleno, colorBorde);
            }
        }

        lienzo2D.dispose();
    }

    /**
     * Calcular rectángulo que abarca solo las celdas de datos, descartando
     * encabezados.
     *
     * @return Rectángulo con límites mínimos y máximos de celdas.
     */
    private Rectangle calcularAreaCeldas() {
        GridBagLayout distribuidor = (GridBagLayout) getLayout();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = 0, maxY = 0;
        for (Component componente : getComponents()) {
            GridBagConstraints gbc = distribuidor.getConstraints(componente);
            if (gbc.gridx > 0 && gbc.gridy > 0) {
                Rectangle bounds = componente.getBounds();
                minX = Math.min(minX, bounds.x);
                minY = Math.min(minY, bounds.y);
                maxX = Math.max(maxX, bounds.x + bounds.width);
                maxY = Math.max(maxY, bounds.y + bounds.height);
            }
        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Dibujar un rectángulo redondeado con color de relleno y borde.
     *
     * @param g2 Contexto gráfico 2D.
     * @param x Coordenada X superior izquierda.
     * @param y Coordenada Y superior izquierda.
     * @param ancho Ancho del rectángulo.
     * @param alto Alto del rectángulo.
     * @param colorRelleno Color de relleno (con transparencia).
     * @param colorBorde Color de borde.
     */
    private void dibujarRectangulo(
            Graphics2D g2,
            int x, int y,
            int ancho, int alto,
            Color colorRelleno,
            Color colorBorde
    ) {
        g2.setColor(colorRelleno);
        g2.fillRoundRect(x, y, ancho, alto, 20, 20);
        g2.setColor(colorBorde);
        g2.drawRoundRect(x, y, ancho, alto, 20, 20);
    }

    /**
     * Encontrar celda en posición específica de la cuadrícula.
     *
     * @param fila Índice de fila en GridBagLayout (incluyendo encabezados).
     * @param columna Índice de columna en GridBagLayout.
     * @return Celda encontrada o null si no existe.
     */
    private Component encontrarCelda(int fila, int columna) {
        GridBagLayout distribuidor = (GridBagLayout) getLayout();
        for (Component componente : getComponents()) {
            GridBagConstraints gbc = distribuidor.getConstraints(componente);
            if (gbc.gridy == fila && gbc.gridx == columna) {
                return componente;
            }
        }
        return null;
    }
}
