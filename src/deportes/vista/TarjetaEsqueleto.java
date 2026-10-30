package deportes.vista;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/** Tarjeta de carga: bloques grises que "laten" mientras llegan los datos. */
class TarjetaEsqueleto extends PanelRedondeado {

    private final Timer latido;
    private float fase = 0f;

    TarjetaEsqueleto() {
        super(null, Estilo.TARJETA, TarjetaProducto.RADIO, true);
        setPreferredSize(TarjetaProducto.TAMANO);
        latido = new Timer(50, e -> {
            fase += 0.08f;
            repaint();
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        latido.start();
    }

    @Override
    public void removeNotify() {
        latido.stop();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Estilo.suavizar(g2);
        int m = margenSombra() / 2;
        int w = getWidth() - margenSombra();
        int alfa = (int) (150 + 80 * Math.sin(fase));
        g2.setColor(new Color(Estilo.ESQUELETO.getRed(), Estilo.ESQUELETO.getGreen(),
                Estilo.ESQUELETO.getBlue(), Math.max(60, Math.min(255, alfa))));

        int r = TarjetaProducto.RADIO;
        int alto = TarjetaProducto.ALTO_IMAGEN;
        Area imagen = new Area(new RoundRectangle2D.Float(m, m, w, alto + r, r, r));
        imagen.intersect(new Area(new Rectangle2D.Float(m, m, w, alto)));
        g2.fill(imagen);

        int x = m + 16;
        int y = m + alto + 16;
        g2.fillRoundRect(x, y, 70, 10, 8, 8);
        g2.fillRoundRect(x, y + 20, (int) ((w - 32) * 0.9), 14, 8, 8);
        g2.fillRoundRect(x, y + 40, (int) ((w - 32) * 0.6), 14, 8, 8);
        for (int i = 0; i < 3; i++) {
            g2.fillRoundRect(x, y + 66 + i * 18, (int) ((w - 32) * (i == 2 ? 0.5 : 0.95)), 10, 8, 8);
        }
        g2.fillRoundRect(x, getHeight() - m - 36, 80, 16, 8, 8);
        g2.dispose();
    }
}
