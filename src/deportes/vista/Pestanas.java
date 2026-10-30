package deportes.vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Selector de categoría en forma de pestañas redondeadas con contador:
 * "Equipo principal 128 · Calzado 38 · Accesorios 53".
 */
class Pestanas extends JComponent {

    private static final int ALTO = 40;
    private static final int SEPARACION = 6;

    private final List<String> nombres = new ArrayList<>();
    private final List<String> contadores = new ArrayList<>();
    private final List<int[]> zonas = new ArrayList<>();
    private int seleccionada = 0;
    private int encima = -1;
    private IntConsumer alCambiar = i -> { };

    Pestanas() {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(600, ALTO));
        MouseAdapter raton = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int i = indiceEn(e.getX());
                if (i >= 0 && i != seleccionada) {
                    seleccionada = i;
                    repaint();
                    alCambiar.accept(i);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int i = indiceEn(e.getX());
                if (i != encima) {
                    encima = i;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                encima = -1;
                repaint();
            }
        };
        addMouseListener(raton);
        addMouseMotionListener(raton);
    }

    void setPestanas(List<String> nombres, List<String> contadores) {
        this.nombres.clear();
        this.nombres.addAll(nombres);
        this.contadores.clear();
        this.contadores.addAll(contadores);
        repaint();
    }

    void setSeleccionada(int indice) {
        seleccionada = indice;
        repaint();
    }

    void alCambiar(IntConsumer accion) {
        this.alCambiar = accion;
    }

    private int indiceEn(int x) {
        for (int i = 0; i < zonas.size(); i++) {
            if (x >= zonas.get(i)[0] && x < zonas.get(i)[1]) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Estilo.suavizar(g2);
        zonas.clear();
        Font fNombre = Estilo.fuente(Font.BOLD, 13.5f);
        Font fContador = Estilo.fuente(Font.BOLD, 11.5f);
        int x = 0;
        for (int i = 0; i < nombres.size(); i++) {
            FontMetrics fm = g2.getFontMetrics(fNombre);
            FontMetrics fc = g2.getFontMetrics(fContador);
            String contador = contadores.get(i);
            int anchoContador = fc.stringWidth(contador) + 14;
            int ancho = 18 + fm.stringWidth(nombres.get(i)) + 8 + anchoContador + 12;
            boolean sel = i == seleccionada;

            g2.setColor(sel ? Estilo.LATERAL : (i == encima ? Estilo.ESQUELETO : Estilo.TARJETA));
            g2.fillRoundRect(x, 2, ancho, ALTO - 4, ALTO - 4, ALTO - 4);
            if (!sel) {
                g2.setColor(Estilo.ESQUELETO);
                g2.drawRoundRect(x, 2, ancho - 1, ALTO - 5, ALTO - 5, ALTO - 5);
            }
            g2.setFont(fNombre);
            g2.setColor(sel ? java.awt.Color.WHITE : Estilo.TEXTO);
            int base = (ALTO + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(nombres.get(i), x + 18, base);

            int cx = x + 18 + fm.stringWidth(nombres.get(i)) + 8;
            g2.setColor(sel ? Estilo.ACENTO : Estilo.ACENTO_SUAVE);
            g2.fillRoundRect(cx, (ALTO - 22) / 2, anchoContador, 22, 22, 22);
            g2.setFont(fContador);
            g2.setColor(sel ? Estilo.LATERAL : Estilo.ACENTO_OSCURO);
            g2.drawString(contador, cx + 7, (ALTO + fc.getAscent() - fc.getDescent()) / 2);

            zonas.add(new int[]{x, x + ancho});
            x += ancho + SEPARACION;
        }
        g2.dispose();
    }
}
