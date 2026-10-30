package deportes.vista;

import deportes.cliente.GestorKitDeportivo;
import deportes.datos.CargadorImagenes;
import deportes.fabrica.FabricaDeportiva;
import deportes.productos.ProductoDeportivo;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Ventana principal. A la izquierda, los deportes disponibles; a la derecha,
 * los productos del deporte elegido, separados en pestañas por categoría
 * (equipo principal, calzado y accesorios).
 *
 * La ventana no conoce ninguna fábrica concreta: recibe el catálogo
 * de deportes ya preparado desde Main.
 */
public class VentanaKit extends JFrame {

    private static final String[] CATEGORIAS = {"Equipo principal", "Calzado", "Accesorios"};
    private static final String URL_CAMBIO = "https://www.exchangerate-api.com";

    private final Map<String, Supplier<FabricaDeportiva>> deportes;
    private final List<String> creditos;
    private final List<ElementoMenu> elementos = new ArrayList<>();
    private final JLabel titulo = new JLabel();
    private final JLabel subtitulo = new JLabel();
    private final Insignia estado = new Insignia();
    private final Pestanas pestanas = new Pestanas();
    private final Cuadricula cuadricula = new Cuadricula();
    private final JScrollPane desplazamiento = new JScrollPane(cuadricula);
    private final ExecutorService descargas = Executors.newFixedThreadPool(6, r -> {
        Thread t = new Thread(r, "imagenes");
        t.setDaemon(true);
        return t;
    });

    private List<List<? extends ProductoDeportivo>> categorias = List.of();
    private SwingWorker<GestorKitDeportivo, Void> cargaEnCurso;
    /** Cambia con cada selección: las descargas de una selección anterior se descartan. */
    private int generacion;

    /**
     * Abre la ventana con el catálogo de deportes indicado.
     *
     * @param creditos líneas que citan el origen de los datos (se muestran en la barra lateral)
     */
    public static void abrir(Map<String, Supplier<FabricaDeportiva>> deportes, List<String> creditos) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> new VentanaKit(deportes, creditos).setVisible(true));
    }

    public VentanaKit(Map<String, Supplier<FabricaDeportiva>> deportes, List<String> creditos) {
        super("Kit Deportivo");
        this.deportes = deportes;
        this.creditos = creditos;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(crearLateral(), BorderLayout.WEST);
        raiz.add(crearContenido(), BorderLayout.CENTER);
        setContentPane(raiz);

        if (deportes.isEmpty()) {
            mostrarVacio();
        } else {
            seleccionar(0);
        }
    }

    // ---------------------------------------------------------------- lateral

    private JComponent crearLateral() {
        JPanel lateral = new JPanel();
        lateral.setBackground(Estilo.LATERAL);
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBorder(BorderFactory.createEmptyBorder(26, 16, 22, 16));
        lateral.setPreferredSize(new Dimension(250, 10));

        JPanel marca = new JPanel(new BorderLayout(12, 0));
        marca.setOpaque(false);
        marca.add(new Logo(), BorderLayout.WEST);
        JPanel nombres = new JPanel(new GridLayout(2, 1));
        nombres.setOpaque(false);
        nombres.add(etiqueta("Kit Deportivo", Font.BOLD, 17f, Color.WHITE));
        nombres.add(etiqueta("Abstract Factory", Font.PLAIN, 12f, Estilo.LATERAL_TEXTO_TENUE));
        marca.add(nombres, BorderLayout.CENTER);
        marca.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        marca.setAlignmentX(LEFT_ALIGNMENT);
        lateral.add(marca);
        lateral.add(Box.createVerticalStrut(34));

        JLabel seccion = etiqueta("DEPORTES", Font.BOLD, 11f, Estilo.LATERAL_TEXTO_TENUE);
        seccion.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
        seccion.setAlignmentX(LEFT_ALIGNMENT);
        lateral.add(seccion);

        int i = 0;
        for (String deporte : deportes.keySet()) {
            ElementoMenu elemento = new ElementoMenu(deporte,
                    Estilo.COLORES_DEPORTE.get(i % Estilo.COLORES_DEPORTE.size()), i);
            elementos.add(elemento);
            lateral.add(elemento);
            lateral.add(Box.createVerticalStrut(4));
            i++;
        }
        if (deportes.isEmpty()) {
            JLabel ninguno = etiqueta("Ninguno todavía", Font.PLAIN, 13f, Estilo.LATERAL_TEXTO_TENUE);
            ninguno.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            ninguno.setAlignmentX(LEFT_ALIGNMENT);
            lateral.add(ninguno);
        }

        lateral.add(Box.createVerticalGlue());
        for (String credito : creditos) {
            JLabel linea = etiqueta("<html><div style='width:165px'>" + credito + "</div></html>",
                    Font.PLAIN, 11f, Estilo.LATERAL_TEXTO_TENUE);
            linea.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 0));
            linea.setAlignmentX(LEFT_ALIGNMENT);
            if (credito.contains("Exchange Rate API")) {
                linea.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                linea.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        TarjetaProducto.abrirEnNavegador(VentanaKit.this, URL_CAMBIO);
                    }
                });
            }
            lateral.add(linea);
        }
        return lateral;
    }

    // -------------------------------------------------------------- contenido

    private JComponent crearContenido() {
        JPanel contenido = new JPanel(new BorderLayout(0, 18));
        contenido.setBackground(Estilo.FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(30, 34, 0, 20));

        JPanel cabecera = new JPanel(new BorderLayout(0, 18));
        cabecera.setOpaque(false);
        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setOpaque(false);
        JPanel textos = new JPanel(new BorderLayout(0, 6));
        textos.setOpaque(false);
        titulo.setFont(Estilo.fuente(Font.BOLD, 30f));
        titulo.setForeground(Estilo.TEXTO);
        subtitulo.setFont(Estilo.fuente(Font.PLAIN, 14.5f));
        subtitulo.setForeground(Estilo.TEXTO_SECUNDARIO);
        textos.add(titulo, BorderLayout.NORTH);
        textos.add(subtitulo, BorderLayout.SOUTH);
        arriba.add(textos, BorderLayout.WEST);
        JPanel derecha = new JPanel(new GridBagLayout());
        derecha.setOpaque(false);
        derecha.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 14));
        derecha.add(estado);
        arriba.add(derecha, BorderLayout.EAST);
        cabecera.add(arriba, BorderLayout.NORTH);
        pestanas.alCambiar(this::mostrarCategoria);
        cabecera.add(pestanas, BorderLayout.SOUTH);
        contenido.add(cabecera, BorderLayout.NORTH);

        desplazamiento.setBorder(null);
        desplazamiento.setOpaque(false);
        desplazamiento.getViewport().setOpaque(false);
        desplazamiento.getVerticalScrollBar().setUnitIncrement(24);
        desplazamiento.getVerticalScrollBar().setUI(new BarraFina());
        desplazamiento.getVerticalScrollBar().setPreferredSize(new Dimension(10, 10));
        desplazamiento.getVerticalScrollBar().setOpaque(false);
        desplazamiento.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contenido.add(desplazamiento, BorderLayout.CENTER);
        return contenido;
    }

    // ------------------------------------------------------------ comportamiento

    private void seleccionar(int indice) {
        for (ElementoMenu e : elementos) {
            e.setSeleccionado(e.indice == indice);
        }
        cargar(elementos.get(indice).deporte);
    }

    /** Crea los productos del deporte en segundo plano, sin bloquear la ventana. */
    private void cargar(String deporte) {
        if (cargaEnCurso != null) {
            cargaEnCurso.cancel(true);
        }
        generacion++;
        titulo.setText(deporte);
        subtitulo.setText("Cargando los productos y traduciéndolos al español…");
        estado.mostrar("Cargando…", new Color(0xE0F2FE), new Color(0x075985));
        pestanas.setPestanas(List.of(CATEGORIAS), List.of("…", "…", "…"));
        pestanas.setSeleccionada(0);
        List<Component> esqueletos = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            esqueletos.add(new TarjetaEsqueleto());
        }
        cuadricula.mostrar(esqueletos);
        irArriba();

        Supplier<FabricaDeportiva> crearFabrica = deportes.get(deporte);
        cargaEnCurso = new SwingWorker<>() {
            @Override
            protected GestorKitDeportivo doInBackground() {
                return new GestorKitDeportivo(crearFabrica.get());
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    return;
                }
                try {
                    mostrarProductos(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    subtitulo.setText("No se pudieron cargar los productos: " + e.getCause().getMessage());
                    estado.mostrar("Error", Estilo.AVISO_SUAVE, Estilo.AVISO);
                    cuadricula.mostrar(List.of());
                }
            }
        };
        cargaEnCurso.execute();
    }

    private void mostrarProductos(GestorKitDeportivo gestor) {
        categorias = List.of(gestor.getEquipos(), gestor.getCalzados(), gestor.getAccesorios());
        List<String> contadores = new ArrayList<>();
        int total = 0;
        boolean completo = true;
        for (List<? extends ProductoDeportivo> lista : categorias) {
            int n = hayProductos(lista) ? lista.size() : 0;
            contadores.add(String.valueOf(n));
            total += n;
            completo &= n > 0;
        }
        pestanas.setPestanas(List.of(CATEGORIAS), contadores);
        subtitulo.setText(total + " productos de este deporte, en tres categorías");
        if (completo) {
            estado.mostrar("" + total + " productos", Estilo.ACENTO_SUAVE, Estilo.ACENTO_OSCURO);
        } else {
            estado.mostrar("Faltan categorías", Estilo.AVISO_SUAVE, Estilo.AVISO);
        }
        pestanas.setSeleccionada(0);
        mostrarCategoria(0);
    }

    private void mostrarCategoria(int indice) {
        if (indice >= categorias.size()) {
            return;
        }
        List<? extends ProductoDeportivo> lista = categorias.get(indice);
        irArriba();
        if (!hayProductos(lista)) {
            cuadricula.mostrarMensaje("No hay productos de esta categoría",
                    lista.isEmpty() ? "" : lista.get(0).getInfo().descripcion());
            return;
        }
        int esta = ++generacion;
        List<Component> tarjetas = new ArrayList<>();
        for (ProductoDeportivo producto : lista) {
            TarjetaProducto tarjeta = new TarjetaProducto(producto);
            tarjetas.add(tarjeta);
            descargas.submit(() -> {
                if (esta != generacion) {
                    return;
                }
                BufferedImage foto = CargadorImagenes.cargar(tarjeta.urlImagen());
                SwingUtilities.invokeLater(() -> tarjeta.setImagen(foto));
            });
        }
        cuadricula.mostrar(tarjetas);
        irArriba();
    }

    /** Vuelve al principio de la lista cuando ya se han colocado las tarjetas nuevas. */
    private void irArriba() {
        SwingUtilities.invokeLater(() -> desplazamiento.getViewport().setViewPosition(new Point(0, 0)));
    }

    private static boolean hayProductos(List<? extends ProductoDeportivo> lista) {
        return !lista.isEmpty() && lista.get(0).getInfo().encontrado();
    }

    private void mostrarVacio() {
        titulo.setText("Bienvenido");
        subtitulo.setText("Elige un deporte para ver su equipamiento");
        estado.setVisible(false);
        pestanas.setVisible(false);
        cuadricula.mostrarMensaje("No hay deportes disponibles",
                "Todavía no se ha registrado ninguna familia de productos.");
    }

    private static JLabel etiqueta(String texto, int estilo, float tamano, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(Estilo.fuente(estilo, tamano));
        l.setForeground(color);
        return l;
    }

    // ------------------------------------------------------- componentes propios

    /**
     * Cuadrícula de tarjetas que se adapta al ancho: tantas columnas como quepan
     * (tarjetas de ancho mínimo fijo) y desplazamiento vertical.
     */
    private static class Cuadricula extends JPanel implements Scrollable {
        private static final int HUECO = 18;
        private static final Insets MARGEN = new Insets(4, 0, 30, 14);

        Cuadricula() {
            super(null);
            setOpaque(false);
        }

        void mostrar(List<Component> componentes) {
            removeAll();
            componentes.forEach(this::add);
            revalidate();
            repaint();
        }

        void mostrarMensaje(String titulo, String detalle) {
            removeAll();
            JPanel caja = new JPanel(new BorderLayout(0, 8));
            caja.setOpaque(false);
            JLabel t = etiqueta(titulo, Font.BOLD, 18f, Estilo.TEXTO);
            t.setHorizontalAlignment(SwingConstants.CENTER);
            JLabel d = etiqueta(detalle, Font.PLAIN, 14f, Estilo.TEXTO_SECUNDARIO);
            d.setHorizontalAlignment(SwingConstants.CENTER);
            caja.add(t, BorderLayout.NORTH);
            caja.add(d, BorderLayout.SOUTH);
            caja.putClientProperty("mensaje", true);
            add(caja);
            revalidate();
            repaint();
        }

        private int columnas(int ancho) {
            int util = ancho - MARGEN.left - MARGEN.right;
            return Math.max(1, (util + HUECO) / (TarjetaProducto.TAMANO.width + HUECO));
        }

        @Override
        public void doLayout() {
            int ancho = getWidth();
            if (getComponentCount() == 1 && getComponent(0) instanceof JComponent c
                    && c.getClientProperty("mensaje") != null) {
                c.setBounds(0, 80, ancho, c.getPreferredSize().height);
                return;
            }
            int cols = columnas(ancho);
            int util = ancho - MARGEN.left - MARGEN.right;
            int anchoTarjeta = (util - (cols - 1) * HUECO) / cols;
            int alto = TarjetaProducto.TAMANO.height;
            for (int i = 0; i < getComponentCount(); i++) {
                int fila = i / cols;
                int col = i % cols;
                getComponent(i).setBounds(MARGEN.left + col * (anchoTarjeta + HUECO),
                        MARGEN.top + fila * (alto + HUECO), anchoTarjeta, alto);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            int ancho = getParent() == null ? 900 : getParent().getWidth();
            int n = getComponentCount();
            if (n == 1 && getComponent(0) instanceof JComponent c && c.getClientProperty("mensaje") != null) {
                return new Dimension(ancho, 300);
            }
            int filas = (n + columnas(ancho) - 1) / columnas(ancho);
            int alto = MARGEN.top + MARGEN.bottom + filas * TarjetaProducto.TAMANO.height
                    + Math.max(0, filas - 1) * HUECO;
            return new Dimension(ancho, alto);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visible, int orientacion, int direccion) {
            return 24;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visible, int orientacion, int direccion) {
            return visible.height - 40;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /** Elemento de la barra lateral: icono con la inicial y nombre del deporte. */
    private class ElementoMenu extends JComponent {
        private final String deporte;
        private final Color color;
        private final int indice;
        private boolean seleccionado;
        private boolean encima;

        ElementoMenu(String deporte, Color color, int indice) {
            this.deporte = deporte;
            this.color = color;
            this.indice = indice;
            setAlignmentX(LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(200, 46));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!seleccionado) {
                        seleccionar(indice);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        void setSeleccionado(boolean valor) {
            seleccionado = valor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Estilo.suavizar(g2);
            int h = getHeight();
            if (seleccionado || encima) {
                g2.setColor(Estilo.LATERAL_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), h, 12, 12);
            }
            if (seleccionado) {
                g2.setColor(Estilo.ACENTO);
                g2.fillRoundRect(0, 10, 4, h - 20, 4, 4);
            }
            int d = 28;
            int y = (h - d) / 2;
            g2.setColor(color);
            g2.fillRoundRect(14, y, d, d, 9, 9);
            g2.setColor(Color.WHITE);
            g2.setFont(Estilo.fuente(Font.BOLD, 13f));
            FontMetrics fm = g2.getFontMetrics();
            String inicial = deporte.substring(0, 1).toUpperCase();
            g2.drawString(inicial, 14 + (d - fm.stringWidth(inicial)) / 2,
                    y + (d + fm.getAscent() - fm.getDescent()) / 2);

            g2.setFont(Estilo.fuente(seleccionado ? Font.BOLD : Font.PLAIN, 14.5f));
            fm = g2.getFontMetrics();
            g2.setColor(seleccionado ? Color.WHITE : Estilo.LATERAL_TEXTO);
            g2.drawString(deporte, 14 + d + 14, (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Logotipo: cuadrado redondeado con una "K". */
    private static class Logo extends JComponent {
        Logo() {
            setPreferredSize(new Dimension(40, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Estilo.suavizar(g2);
            g2.setColor(Estilo.ACENTO);
            g2.fillRoundRect(0, 0, 40, 40, 12, 12);
            g2.setColor(Estilo.LATERAL);
            g2.setFont(Estilo.fuente(Font.BOLD, 20f));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("K", (40 - fm.stringWidth("K")) / 2, (40 + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Barra de desplazamiento fina y redondeada, sin flechas. */
    private static class BarraFina extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(0xCBD5E1);
        }

        @Override
        protected JButton createDecreaseButton(int orientacion) {
            return botonVacio();
        }

        @Override
        protected JButton createIncreaseButton(int orientacion) {
            return botonVacio();
        }

        private static JButton botonVacio() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            // pista transparente
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            Estilo.suavizar(g2);
            g2.setColor(isDragging ? new Color(0x94A3B8) : thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6);
            g2.dispose();
        }
    }

    /** Píldora de estado en la cabecera ("219 productos", "Cargando..."). */
    private static class Insignia extends JLabel {
        private Color fondo = Color.WHITE;

        Insignia() {
            setFont(Estilo.fuente(Font.BOLD, 12.5f));
            // Hueco a la izquierda para el punto, que se dibuja (no todas las fuentes tienen "●")
            setBorder(BorderFactory.createEmptyBorder(7, 30, 7, 14));
        }

        void mostrar(String texto, Color fondo, Color color) {
            this.fondo = fondo;
            setText(texto);
            setForeground(color);
            setVisible(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Estilo.suavizar(g2);
            g2.setColor(fondo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setColor(getForeground());
            g2.fillOval(14, getHeight() / 2 - 4, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
