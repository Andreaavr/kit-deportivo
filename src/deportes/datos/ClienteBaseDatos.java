package deportes.datos;

import deportes.productos.InfoMaterial;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Estrategia que obtiene los productos de la base de datos local
 * ({@code datos/catalogo.db}), creada por {@link ImportadorCatalogo}.
 * Funciona sin conexión y siempre devuelve lo mismo para el mismo criterio.
 * Si la importación guardó un tipo de cambio, los precios se dan en euros.
 */
public class ClienteBaseDatos implements FuenteDatosMaterial {

    private static final Locale ES = Locale.forLanguageTag("es-ES");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Columna con tipo y naturaleza, en minúsculas y rodeada de espacios para buscar palabras completas. */
    private static final String COLUMNA_TIPO =
            "(' ' || replace(replace(lower(tipo || ' ' || naturaleza), '-', ' '), '/', ' ') || ' ')";

    private final Path archivo;
    private Metadatos metadatos;

    /** Consulta SQL con sus parámetros, en el orden de los '?'. */
    record Consulta(String sql, List<String> parametros) {
    }

    /** Datos de la tabla {@code importacion}. */
    private record Metadatos(String tienda, String moneda, LocalDate fecha, int productos,
                             Double tasaEur, LocalDate fechaTasa) {
    }

    public ClienteBaseDatos(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public List<InfoMaterial> buscar(Criterio criterio) {
        String material = criterio.busqueda();
        if (!Files.exists(archivo)) {
            return List.of(InfoMaterial.aviso(material, "No se encuentra la base de datos (" + archivo
                    + "). Ejecuta ImportadorCatalogo para crearla."));
        }
        Consulta consulta = construirConsulta(criterio);
        List<InfoMaterial> productos = new ArrayList<>();
        try (Connection c = conectar();
             PreparedStatement ps = c.prepareStatement(consulta.sql())) {
            Metadatos meta = metadatos(c);
            for (int i = 0; i < consulta.parametros().size(); i++) {
                ps.setString(i + 1, consulta.parametros().get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double precio = rs.getDouble("precio");
                    String textoPrecio = rs.wasNull() ? null : formatearPrecio(precio, meta);
                    productos.add(new InfoMaterial(
                            rs.getString("titulo"),
                            vacioANull(rs.getString("marca")),
                            rs.getString("descripcion"),
                            rs.getString("extracto"),
                            textoPrecio,
                            rs.getString("url_imagen"),
                            rs.getString("url_enlace")));
                }
            }
        } catch (SQLException e) {
            return List.of(InfoMaterial.aviso(material, "Error al leer la base de datos: " + e.getMessage()));
        }
        return productos.isEmpty() ? List.of(InfoMaterial.noEncontrado(material)) : productos;
    }

    /**
     * Construye la consulta para un criterio:
     * <ul>
     *   <li>solo productos disponibles y no genéricos (pocas etiquetas de deporte);</li>
     *   <li>deporte: alguna palabra aparece en la columna {@code deportes};</li>
     *   <li>tipo: alguna palabra o expresión aparece <b>completa</b> en {@code tipo} o
     *       {@code naturaleza} ("shoes" encaja con "Sport shoes", pero no con "Shoe bag");</li>
     *   <li>se ordena por cuántas palabras de la búsqueda aparecen en el título.</li>
     * </ul>
     */
    static Consulta construirConsulta(Criterio criterio) {
        List<String> parametros = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT titulo, marca, descripcion, extracto, precio, url_imagen, url_enlace
                FROM producto
                WHERE disponible = 1
                  AND num_deportes <=""").append(" ").append(ClienteShopify.MAX_DEPORTES);

        if (!criterio.deporte().isEmpty()) {
            List<String> condiciones = new ArrayList<>();
            for (String d : criterio.deporte()) {
                condiciones.add("lower(deportes) LIKE ?");
                parametros.add("%" + d.toLowerCase(Locale.ROOT) + "%");
            }
            sql.append("\n  AND (").append(String.join(" OR ", condiciones)).append(')');
        }
        if (!criterio.tipo().isEmpty()) {
            List<String> condiciones = new ArrayList<>();
            for (String t : criterio.tipo()) {
                condiciones.add(COLUMNA_TIPO + " LIKE ?");
                parametros.add("% " + t.toLowerCase(Locale.ROOT).replace('-', ' ') + " %");
            }
            sql.append("\n  AND (").append(String.join(" OR ", condiciones)).append(')');
        }

        List<String> sumandos = new ArrayList<>();
        for (String p : criterio.busqueda().toLowerCase(Locale.ROOT).split("\\s+")) {
            if (p.length() >= 3) {
                sumandos.add("(lower(titulo) LIKE ?)");
                parametros.add("%" + p + "%");
            }
        }
        sql.append("\nORDER BY ");
        if (!sumandos.isEmpty()) {
            sql.append(String.join(" + ", sumandos)).append(" DESC, ");
        }
        sql.append("id");
        return new Consulta(sql.toString(), parametros);
    }

    /** Texto para citar el origen de los datos en la ventana. */
    public String describirOrigen() {
        Metadatos m = metadatosSinError();
        if (m == null) {
            return "Base de datos local (sin crear)";
        }
        return String.format(ES, "%,d productos de %s, importados el %s",
                m.productos(), m.tienda().replaceFirst("^https?://", ""), m.fecha().format(FECHA));
    }

    /** Texto que explica en qué moneda están los precios. */
    public String describirPrecios() {
        Metadatos m = metadatosSinError();
        if (m == null) {
            return "";
        }
        if (m.tasaEur() == null) {
            return "Precios en " + m.moneda() + " (no se pudo obtener el cambio a euros)";
        }
        return String.format(ES, "Precios convertidos de %s a euros (1 %s = %.4f €, cambio del %s)",
                m.moneda(), m.moneda(), m.tasaEur(), m.fechaTasa().format(FECHA));
    }

    static String formatearPrecio(double precio, String moneda, Double tasaEur) {
        if (tasaEur != null) {
            return String.format(ES, "%,.2f €", precio * tasaEur);
        }
        return String.format(ES, "%,.2f %s", precio, moneda);
    }

    private String formatearPrecio(double precio, Metadatos m) {
        return formatearPrecio(precio, m == null ? "" : m.moneda(), m == null ? null : m.tasaEur());
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + archivo);
    }

    private synchronized Metadatos metadatos(Connection c) throws SQLException {
        if (metadatos == null) {
            try {
                metadatos = leerMetadatos(c, "tasa_eur, fecha_tasa");
            } catch (SQLException e) {
                // Base de datos importada con una versión anterior, sin el cambio a euros:
                // los precios se muestran en la moneda original hasta volver a importar.
                metadatos = leerMetadatos(c, "NULL AS tasa_eur, NULL AS fecha_tasa");
            }
        }
        return metadatos;
    }

    private static Metadatos leerMetadatos(Connection c, String columnasCambio) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT tienda, moneda, fecha, productos, " + columnasCambio + " FROM importacion LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return null;
            }
            double tasa = rs.getDouble("tasa_eur");
            Double tasaEur = rs.wasNull() ? null : tasa;
            String fechaTasa = rs.getString("fecha_tasa");
            return new Metadatos(rs.getString("tienda"), rs.getString("moneda"),
                    LocalDate.parse(rs.getString("fecha")), rs.getInt("productos"), tasaEur,
                    fechaTasa == null ? null : LocalDate.parse(fechaTasa));
        }
    }

    private Metadatos metadatosSinError() {
        if (!Files.exists(archivo)) {
            return null;
        }
        try (Connection c = conectar()) {
            return metadatos(c);
        } catch (SQLException | RuntimeException e) {
            return null;
        }
    }

    private static String vacioANull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}
