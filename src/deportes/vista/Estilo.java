package deportes.vista;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/** Colores, fuentes y utilidades de dibujo comunes a toda la interfaz. */
final class Estilo {

    // Paleta
    static final Color FONDO = new Color(0xF1F5F9);
    static final Color LATERAL = new Color(0x0F172A);
    static final Color LATERAL_HOVER = new Color(0x1E293B);
    static final Color LATERAL_TEXTO = new Color(0xCBD5E1);
    static final Color LATERAL_TEXTO_TENUE = new Color(0x64748B);
    static final Color ACENTO = new Color(0x14B8A6);
    static final Color ACENTO_OSCURO = new Color(0x0F766E);
    static final Color ACENTO_SUAVE = new Color(0xCCFBF1);
    static final Color TARJETA = Color.WHITE;
    static final Color TEXTO = new Color(0x0F172A);
    static final Color TEXTO_SECUNDARIO = new Color(0x475569);
    static final Color TEXTO_TENUE = new Color(0x94A3B8);
    static final Color ESQUELETO = new Color(0xE2E8F0);
    static final Color AVISO_SUAVE = new Color(0xFEF3C7);
    static final Color AVISO = new Color(0x92400E);

    /** Un color por deporte para su icono en la barra lateral. */
    static final List<Color> COLORES_DEPORTE = List.of(
            new Color(0x14B8A6), new Color(0xF97316), new Color(0x6366F1),
            new Color(0xEC4899), new Color(0x22C55E), new Color(0xEAB308));

    static final String FUENTE = elegirFuente(
            "Inter", "Segoe UI", "Ubuntu", "Cantarell", "Noto Sans", "DejaVu Sans");

    private Estilo() {
    }

    static Font fuente(int estilo, float tamano) {
        return new Font(FUENTE, estilo, Math.round(tamano)).deriveFont(tamano);
    }

    static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    private static String elegirFuente(String... preferidas) {
        List<String> instaladas = Arrays.asList(GraphicsEnvironment
                .getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String f : preferidas) {
            if (instaladas.contains(f)) {
                return f;
            }
        }
        return Font.SANS_SERIF;
    }
}
