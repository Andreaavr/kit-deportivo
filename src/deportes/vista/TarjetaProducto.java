package deportes.vista;

import deportes.productos.InfoMaterial;
import deportes.productos.ProductoDeportivo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URI;

/**
 * Tarjeta compacta de un producto: foto, marca, nombre, descripción corta,
 * precio y enlace a la página del producto. La foto se coloca después,
 * cuando termina de descargarse.
 */
class TarjetaProducto extends PanelRedondeado {

    static final int RADIO = 16;
    static final int ALTO_IMAGEN = 170;
    static final Dimension TAMANO = new Dimension(250, 384);
    private static final int MAX_TITULO = 75;
    private static final int MAX_DESCRIPCION = 125;

    private final ImagenCabecera imagen = new ImagenCabecera(ALTO_IMAGEN, RADIO);
    private final InfoMaterial info;

    TarjetaProducto(ProductoDeportivo producto) {
        super(new BorderLayout(), Estilo.TARJETA, RADIO, true);
        this.info = producto.getInfo();
        int m = margenSombra() / 2;
        setBorder(BorderFactory.createEmptyBorder(m, m, m, m));
        setPreferredSize(TAMANO);

        add(imagen, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel();
        cuerpo.setOpaque(false);
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setBorder(BorderFactory.createEmptyBorder(14, 16, 4, 16));
        if (info.marca() != null) {
            JLabel marca = new JLabel(info.marca().toUpperCase());
            marca.setFont(Estilo.fuente(Font.BOLD, 11f));
            marca.setForeground(Estilo.TEXTO_TENUE);
            cuerpo.add(izquierda(marca));
            cuerpo.add(Box.createVerticalStrut(4));
        }
        cuerpo.add(texto(recortar(mayuscula(info.titulo()), MAX_TITULO),
                Estilo.fuente(Font.BOLD, 15f), Estilo.TEXTO));
        cuerpo.add(Box.createVerticalStrut(6));
        cuerpo.add(texto(recortar(info.descripcion(), MAX_DESCRIPCION),
                Estilo.fuente(Font.PLAIN, 12.5f), Estilo.TEXTO_SECUNDARIO));

        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(cuerpo, BorderLayout.NORTH);
        add(envoltorio, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createEmptyBorder(2, 16, 12, 10));
        if (info.precio() != null) {
            JLabel precio = new JLabel(info.precio());
            precio.setFont(Estilo.fuente(Font.BOLD, 17f));
            precio.setForeground(Estilo.TEXTO);
            pie.add(precio, BorderLayout.WEST);
        }
        pie.add(crearEnlace(info.urlEnlace()), BorderLayout.EAST);
        add(pie, BorderLayout.SOUTH);
        setToolTipText(info.titulo());
    }

    /** URL de la foto que hay que descargar (o null). */
    String urlImagen() {
        return info.urlImagen();
    }

    /** Coloca la foto ya descargada. */
    void setImagen(BufferedImage foto) {
        imagen.setImagen(foto);
    }

    private JComponent crearEnlace(String url) {
        if (url == null) {
            JLabel sinEnlace = new JLabel("No disponible");
            sinEnlace.setFont(Estilo.fuente(Font.PLAIN, 12.5f));
            sinEnlace.setForeground(Estilo.TEXTO_TENUE);
            return sinEnlace;
        }
        JButton enlace = new JButton("Ver en la tienda »");
        enlace.setFont(Estilo.fuente(Font.BOLD, 12.5f));
        enlace.setForeground(Estilo.ACENTO_OSCURO);
        enlace.setContentAreaFilled(false);
        enlace.setBorderPainted(false);
        enlace.setFocusPainted(false);
        enlace.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        enlace.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        enlace.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                enlace.setForeground(Estilo.ACENTO);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                enlace.setForeground(Estilo.ACENTO_OSCURO);
            }
        });
        enlace.addActionListener(e -> abrirEnNavegador(this, url));
        return enlace;
    }

    static void abrirEnNavegador(Component padre, String url) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                return;
            }
        } catch (Exception e) {
            // Si falla, se muestra el enlace para copiarlo
        }
        JTextField campo = new JTextField(url);
        campo.setEditable(false);
        JOptionPane.showMessageDialog(padre, campo, "Abre este enlace en tu navegador",
                JOptionPane.INFORMATION_MESSAGE);
    }

    static JTextArea texto(String contenido, Font fuente, Color color) {
        JTextArea area = new JTextArea(contenido);
        area.setFont(fuente);
        area.setForeground(color);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder());
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        return area;
    }

    static <T extends JComponent> T izquierda(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    static String recortar(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        int corte = s.lastIndexOf(' ', max);
        return s.substring(0, corte > 0 ? corte : max).replaceAll("[,;:.\\-]$", "") + "…";
    }

    private static String mayuscula(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
