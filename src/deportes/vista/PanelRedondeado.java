package deportes.vista;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Panel con esquinas redondeadas y una sombra suave. */
class PanelRedondeado extends JPanel {

    private final int radio;
    private final boolean sombra;

    PanelRedondeado(LayoutManager layout, Color fondo, int radio, boolean sombra) {
        super(layout);
        this.radio = radio;
        this.sombra = sombra;
        setBackground(fondo);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Estilo.suavizar(g2);
        int margen = sombra ? 6 : 0;
        int ancho = getWidth() - margen;
        int alto = getHeight() - margen;
        if (sombra) {
            for (int i = margen; i > 0; i--) {
                g2.setColor(new Color(15, 23, 42, 4));
                g2.fill(new RoundRectangle2D.Float(margen / 2f - i / 2f + 1, margen / 2f - i / 2f + 3,
                        ancho + i, alto + i, radio + i, radio + i));
            }
        }
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(margen / 2f, margen / 2f, ancho, alto, radio, radio));
        g2.dispose();
        super.paintComponent(g);
    }

    /** Espacio que ocupa la sombra, para dejarlo libre en el contenido. */
    int margenSombra() {
        return sombra ? 6 : 0;
    }
}
