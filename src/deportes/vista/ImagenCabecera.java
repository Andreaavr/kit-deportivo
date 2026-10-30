package deportes.vista;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Foto de la parte superior de la tarjeta: rellena el hueco manteniendo la
 * proporción (sobre fondo blanco, como en las tiendas) y con las esquinas de
 * arriba redondeadas. La imagen puede llegar más tarde ({@link #setImagen}).
 */
class ImagenCabecera extends JComponent {

    private final int radio;
    private BufferedImage imagen;
    private boolean cargando = true;

    ImagenCabecera(int alto, int radio) {
        this.radio = radio;
        setPreferredSize(new Dimension(10, alto));
    }

    /** Muestra la imagen descargada (o "sin imagen" si es null). */
    void setImagen(BufferedImage imagen) {
        this.imagen = imagen;
        this.cargando = false;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Estilo.suavizar(g2);
        int w = getWidth();
        int h = getHeight();

        Area forma = new Area(new RoundRectangle2D.Float(0, 0, w, h + radio, radio, radio));
        forma.intersect(new Area(new Rectangle2D.Float(0, 0, w, h)));
        g2.setClip((Shape) forma);

        if (imagen == null) {
            g2.setColor(Estilo.ESQUELETO);
            g2.fillRect(0, 0, w, h);
            g2.setColor(Estilo.TEXTO_TENUE);
            g2.setFont(Estilo.fuente(Font.PLAIN, 12.5f));
            FontMetrics fm = g2.getFontMetrics();
            String texto = cargando ? "Cargando imagen…" : "Sin imagen disponible";
            g2.drawString(texto, (w - fm.stringWidth(texto)) / 2, h / 2 + fm.getAscent() / 2 - 2);
        } else {
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);
            int margen = 10;
            double escala = Math.min((double) (w - 2 * margen) / imagen.getWidth(),
                    (double) (h - 2 * margen) / imagen.getHeight());
            int iw = (int) Math.round(imagen.getWidth() * escala);
            int ih = (int) Math.round(imagen.getHeight() * escala);
            g2.drawImage(imagen, (w - iw) / 2, (h - ih) / 2, iw, ih, null);
        }
        g2.dispose();
    }
}
