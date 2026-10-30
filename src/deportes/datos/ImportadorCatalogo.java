package deportes.datos;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Herramienta que crea la base de datos local del proyecto: descarga el catálogo
 * completo de la tienda ({@code /products.json}, página a página) y lo guarda en
 * un archivo SQLite ({@code datos/catalogo.db}). También guarda el tipo de cambio
 * a euros del día (ExchangeRate-API) y conserva las traducciones ya hechas.
 *
 * Se ejecuta una vez, o cuando se quieran actualizar precios:
 * <pre>
 *   mvn compile exec:java -Dexec.mainClass=deportes.datos.ImportadorCatalogo
 * </pre>
 * Argumentos opcionales: [urlTienda] [moneda] [archivoDestino].
 */
public class ImportadorCatalogo {

    private static final int POR_PAGINA = 250;
    private static final int MAX_PAGINAS = 200;
    /** Pausa entre páginas para no cargar la tienda. */
    private static final long PAUSA_MS = 1000;

    static final String ESQUEMA = """
            DROP TABLE IF EXISTS producto;
            DROP TABLE IF EXISTS importacion;
            CREATE TABLE producto (
                id              INTEGER PRIMARY KEY,
                titulo          TEXT    NOT NULL,
                marca           TEXT    NOT NULL DEFAULT '',
                tipo            TEXT    NOT NULL DEFAULT '',
                naturaleza      TEXT    NOT NULL DEFAULT '',
                subdepartamento TEXT    NOT NULL DEFAULT '',
                deportes        TEXT    NOT NULL DEFAULT '',
                num_deportes    INTEGER NOT NULL,
                descripcion     TEXT    NOT NULL,
                extracto        TEXT,
                precio          REAL,
                url_imagen      TEXT,
                url_enlace      TEXT    NOT NULL,
                disponible      INTEGER NOT NULL
            );
            CREATE TABLE importacion (
                tienda     TEXT    NOT NULL,
                moneda     TEXT    NOT NULL,
                fecha      TEXT    NOT NULL,
                productos  INTEGER NOT NULL,
                tasa_eur   REAL,
                fecha_tasa TEXT
            );
            """ + TraductorConMemoria.TABLA;

    /** Tipo de cambio diario, sin clave. Exige citar: "Rates By Exchange Rate API". */
    private static final String URL_CAMBIO = "https://open.er-api.com/v6/latest/";

    private static final String INSERTAR = """
            INSERT OR REPLACE INTO producto (id, titulo, marca, tipo, naturaleza, subdepartamento,
                deportes, num_deportes, descripcion, extracto, precio, url_imagen, url_enlace, disponible)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    /** Un producto tal como se guarda en la tabla {@code producto}. */
    record Fila(long id, String titulo, String marca, String tipo, String naturaleza,
                String subdepartamento, String deportes, int numDeportes, String descripcion,
                String extracto, Double precio, String urlImagen, String urlEnlace, boolean disponible) {
    }

    private final String tienda;
    private final String moneda;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public ImportadorCatalogo(String tienda, String moneda) {
        this.tienda = tienda.endsWith("/") ? tienda.substring(0, tienda.length() - 1) : tienda;
        this.moneda = moneda;
    }

    public static void main(String[] args) throws Exception {
        String tienda = args.length > 0 ? args[0] : "https://decathlon.qa";
        String moneda = args.length > 1 ? args[1] : "QAR";
        Path destino = Path.of(args.length > 2 ? args[2] : "datos/catalogo.db");
        new ImportadorCatalogo(tienda, moneda).importar(destino);
    }

    /** Descarga todas las páginas del catálogo y las guarda en {@code destino}. */
    public void importar(Path destino) throws IOException, InterruptedException, SQLException {
        System.out.println("Importando el catálogo de " + tienda + " ...");
        List<Fila> filas = new ArrayList<>();
        for (int pagina = 1; pagina <= MAX_PAGINAS; pagina++) {
            String json = descargar(tienda + "/products.json?limit=" + POR_PAGINA + "&page=" + pagina);
            List<Fila> nuevas = leerPagina(json, tienda);
            if (nuevas.isEmpty()) {
                break;
            }
            filas.addAll(nuevas);
            System.out.printf("  Página %d: %d productos (total: %d)%n", pagina, nuevas.size(), filas.size());
            Thread.sleep(PAUSA_MS);
        }
        Double tasaEur = obtenerTasaEur();
        System.out.println(tasaEur == null
                ? "No se pudo obtener el cambio a euros: los precios se mostrarán en " + moneda
                : "Cambio: 1 " + moneda + " = " + tasaEur + " EUR");
        guardar(filas, tasaEur, destino);
        System.out.println("Base de datos creada: " + destino.toAbsolutePath()
                + " (" + filas.size() + " productos)");
    }

    /** Tipo de cambio de la moneda de la tienda a euros, o null si no se puede obtener. */
    private Double obtenerTasaEur() {
        try {
            return leerTasaEur(descargar(URL_CAMBIO + moneda));
        } catch (IOException | RuntimeException e) {
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /** Lee {@code rates.EUR} de la respuesta de ExchangeRate-API. */
    static Double leerTasaEur(String json) {
        JsonObject raiz = JsonParser.parseString(json).getAsJsonObject();
        JsonElement resultado = raiz.get("result");
        if (resultado == null || !"success".equals(resultado.getAsString())) {
            return null;
        }
        JsonObject tasas = raiz.getAsJsonObject("rates");
        if (tasas == null || tasas.get("EUR") == null) {
            return null;
        }
        return tasas.get("EUR").getAsDouble();
    }

    private String descargar(String url) throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", ClienteShopify.USER_AGENT)
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> respuesta =
                http.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (respuesta.statusCode() != 200) {
            throw new IOException("La tienda respondió " + respuesta.statusCode() + " para " + url);
        }
        return respuesta.body();
    }

    /** Convierte una página de {@code /products.json} en filas de la base de datos. */
    static List<Fila> leerPagina(String json, String tienda) {
        List<Fila> filas = new ArrayList<>();
        JsonArray productos = JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("products");
        if (productos == null) {
            return filas;
        }
        for (JsonElement e : productos) {
            JsonObject p = e.getAsJsonObject();
            List<String> etiquetas = ClienteShopify.etiquetas(p);
            List<String> deportes = valores(etiquetas, "SPORT_VALUE:");
            String[] textos = ClienteShopify.descripcionYExtracto(ClienteShopify.texto(p, "body_html"));

            Double precio = null;
            boolean disponible = false;
            JsonArray variantes = p.getAsJsonArray("variants");
            if (variantes != null && !variantes.isEmpty()) {
                JsonObject v = variantes.get(0).getAsJsonObject();
                try {
                    precio = Double.valueOf(ClienteShopify.texto(v, "price"));
                } catch (NumberFormatException ignorado) {
                    // sin precio
                }
                for (JsonElement ve : variantes) {
                    JsonElement d = ve.getAsJsonObject().get("available");
                    disponible |= d == null || d.isJsonNull() || d.getAsBoolean();
                }
            }
            String imagen = null;
            JsonArray imagenes = p.getAsJsonArray("images");
            if (imagenes != null && !imagenes.isEmpty()) {
                imagen = ClienteShopify.texto(imagenes.get(0).getAsJsonObject(), "src");
            }

            filas.add(new Fila(
                    p.get("id").getAsLong(),
                    ClienteShopify.texto(p, "title"),
                    ClienteShopify.texto(p, "vendor"),
                    ClienteShopify.texto(p, "product_type"),
                    primero(valores(etiquetas, "NATURE_VALUE:")),
                    primero(valores(etiquetas, "SUB_DEPARTMENT_VALUE:")),
                    String.join("|", deportes),
                    deportes.size(),
                    textos[0],
                    textos[1],
                    precio,
                    imagen == null || imagen.isEmpty() ? null : imagen,
                    tienda + "/products/" + ClienteShopify.texto(p, "handle"),
                    disponible));
        }
        return filas;
    }

    private void guardar(List<Fila> filas, Double tasaEur, Path destino) throws IOException, SQLException {
        Path carpeta = destino.toAbsolutePath().getParent();
        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }
        // Se escribe en un archivo temporal y se reemplaza al final: si algo
        // falla a mitad, la base de datos anterior sigue intacta.
        Path temporal = destino.resolveSibling(destino.getFileName() + ".tmp");
        Files.deleteIfExists(temporal);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + temporal)) {
            try (Statement st = c.createStatement()) {
                for (String sql : ESQUEMA.split(";")) {
                    if (!sql.isBlank()) {
                        st.execute(sql);
                    }
                }
            }
            conservarTraducciones(c, destino);
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(INSERTAR)) {
                for (Fila f : filas) {
                    ps.setLong(1, f.id());
                    ps.setString(2, f.titulo());
                    ps.setString(3, f.marca());
                    ps.setString(4, f.tipo());
                    ps.setString(5, f.naturaleza());
                    ps.setString(6, f.subdepartamento());
                    ps.setString(7, f.deportes());
                    ps.setInt(8, f.numDeportes());
                    ps.setString(9, f.descripcion());
                    ps.setString(10, f.extracto());
                    if (f.precio() == null) {
                        ps.setNull(11, Types.REAL);
                    } else {
                        ps.setDouble(11, f.precio());
                    }
                    ps.setString(12, f.urlImagen());
                    ps.setString(13, f.urlEnlace());
                    ps.setInt(14, f.disponible() ? 1 : 0);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO importacion VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, tienda);
                ps.setString(2, moneda);
                ps.setString(3, LocalDate.now().toString());
                ps.setInt(4, filas.size());
                if (tasaEur == null) {
                    ps.setNull(5, Types.REAL);
                    ps.setNull(6, Types.VARCHAR);
                } else {
                    ps.setDouble(5, tasaEur);
                    ps.setString(6, LocalDate.now().toString());
                }
                ps.executeUpdate();
            }
            c.commit();
        }
        Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
    }

    /** Copia las traducciones de la base de datos anterior, para no volver a pedirlas. */
    private static void conservarTraducciones(Connection nueva, Path anterior) {
        if (!Files.exists(anterior)) {
            return;
        }
        try (Statement st = nueva.createStatement()) {
            st.execute("ATTACH DATABASE '" + anterior.toAbsolutePath().toString().replace("'", "''") + "' AS anterior");
            try (ResultSet rs = st.executeQuery(
                    "SELECT count(*) FROM anterior.sqlite_master WHERE type = 'table' AND name = 'traduccion'")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    int copiadas = st.executeUpdate("INSERT OR IGNORE INTO traduccion SELECT * FROM anterior.traduccion");
                    System.out.println("Traducciones conservadas: " + copiadas);
                }
            }
            st.execute("DETACH DATABASE anterior");
        } catch (SQLException e) {
            System.out.println("No se pudieron conservar las traducciones anteriores: " + e.getMessage());
        }
    }

    private static List<String> valores(List<String> etiquetas, String prefijo) {
        List<String> lista = new ArrayList<>();
        for (String e : etiquetas) {
            if (e.startsWith(prefijo)) {
                String valor = e.substring(prefijo.length()).trim();
                if (!valor.isEmpty() && !lista.contains(valor)) {
                    lista.add(valor);
                }
            }
        }
        return lista;
    }

    private static String primero(List<String> lista) {
        return lista.isEmpty() ? "" : lista.get(0);
    }
}
