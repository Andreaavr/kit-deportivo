package deportes.datos;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import deportes.productos.InfoMaterial;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Estrategia que obtiene productos reales (nombre, marca, descripción, precio,
 * foto y enlace) de una tienda online hecha con Shopify, usando su búsqueda
 * pública: {@code /search/suggest.json}. No necesita registro ni claves.
 *
 * Aviso: es un punto de acceso público de la tienda, no una API oficial
 * para terceros. Se usa con pocas peticiones (y con caché) y citando la fuente.
 */
public class ClienteShopify implements FuenteDatosMaterial {

    /** Identifica la aplicación en cada petición. */
    static final String USER_AGENT =
            "KitDeportivo/1.0 (practica academica; https://github.com/Andreaavr/kit-deportivo)";
    private static final int RESULTADOS = 10;
    /**
     * Productos genéricos (cables, camisetas básicas...) llevan las etiquetas de
     * decenas de deportes a la vez; se descartan para que no pasen como
     * material específico. Los productos normales tienen como mucho unos 6.
     */
    static final int MAX_DEPORTES = 8;
    private static final Pattern PARRAFO = Pattern.compile("<p[^>]*>(.*?)</p>", Pattern.DOTALL);

    private final String urlTienda;
    private final String moneda;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * @param urlTienda dirección de la tienda, p. ej. "https://decathlon.qa"
     * @param moneda    moneda de los precios de esa tienda, p. ej. "QAR"
     */
    public ClienteShopify(String urlTienda, String moneda) {
        this.urlTienda = urlTienda.endsWith("/") ? urlTienda.substring(0, urlTienda.length() - 1) : urlTienda;
        this.moneda = moneda;
    }

    /**
     * Busca en la tienda y devuelve los productos disponibles que son del
     * deporte y del tipo indicados en el criterio (como mucho los que devuelve
     * la búsqueda de la tienda). Los precios quedan en la moneda de la tienda.
     */
    @Override
    public List<InfoMaterial> buscar(Criterio criterio) {
        String material = criterio.busqueda();
        try {
            String url = urlTienda + "/search/suggest.json?q=" + codificar(material)
                    + "&resources%5Btype%5D=product&resources%5Blimit%5D=" + RESULTADOS;
            HttpRequest peticion = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> respuesta =
                    http.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (respuesta.statusCode() != 200) {
                return List.of(InfoMaterial.noEncontrado(material));
            }
            List<InfoMaterial> productos = new ArrayList<>();
            for (JsonObject p : elegirProductos(respuesta.body(), criterio)) {
                productos.add(leerProducto(p));
            }
            return productos.isEmpty() ? List.of(InfoMaterial.noEncontrado(material)) : productos;

        } catch (IOException | IllegalArgumentException e) {
            return List.of(InfoMaterial.sinConexion(material));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return List.of(InfoMaterial.sinConexion(material));
        }
    }

    /** El primer producto que cumple el criterio, o null. */
    static JsonObject elegirProducto(String json, Criterio criterio) {
        List<JsonObject> lista = elegirProductos(json, criterio);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /**
     * Productos disponibles y no genéricos que cumplen las dos condiciones:
     * <ul>
     *   <li>deporte: alguna palabra aparece en su nombre, tipo o etiquetas;</li>
     *   <li>tipo: alguna palabra o expresión aparece <b>completa</b> en su tipo
     *       ({@code type}) o su naturaleza ({@code NATURE_VALUE}).</li>
     * </ul>
     */
    static List<JsonObject> elegirProductos(String json, Criterio criterio) {
        List<JsonObject> elegidos = new ArrayList<>();
        JsonObject raiz = JsonParser.parseString(json).getAsJsonObject();
        JsonObject recursos = raiz.getAsJsonObject("resources");
        if (recursos == null || recursos.getAsJsonObject("results") == null) {
            return elegidos;
        }
        JsonArray productos = recursos.getAsJsonObject("results").getAsJsonArray("products");
        if (productos == null) {
            return elegidos;
        }
        for (JsonElement e : productos) {
            JsonObject p = e.getAsJsonObject();
            boolean disponible = !p.has("available") || p.get("available").getAsBoolean();
            if (disponible && numeroDeportes(etiquetas(p)) <= MAX_DEPORTES && contieneAlguna(textoDeporte(p), criterio.deporte())
                    && contienePalabraCompleta(textoTipo(p), criterio.tipo())) {
                elegidos.add(p);
            }
        }
        return elegidos;
    }

    /** Convierte un producto de la búsqueda en un InfoMaterial. */
    InfoMaterial leerProducto(JsonObject p) {
        String[] textos = descripcionYExtracto(texto(p, "body"));
        String imagen = texto(p, "image");
        return new InfoMaterial(
                texto(p, "title"),
                vacioANull(texto(p, "vendor")),
                textos[0],
                textos[1],
                formatearPrecio(texto(p, "price"), moneda),
                imagen.isEmpty() ? null : imagen,
                urlTienda + "/products/" + texto(p, "handle"));
    }

    /**
     * Saca de la ficha HTML de un producto la descripción (primer párrafo) y un
     * texto comercial adicional (el último párrafo largo antes de las
     * características), o null si no lo hay.
     */
    static String[] descripcionYExtracto(String cuerpoHtml) {
        List<String> parrafos = parrafos(cuerpoHtml.split("<div", 2)[0]);
        String descripcion = parrafos.isEmpty() ? "(Sin descripción)" : parrafos.get(0);
        String extracto = null;
        for (int i = parrafos.size() - 1; i > 0; i--) {
            String candidato = parrafos.get(i);
            if (!candidato.equals(descripcion) && candidato.length() > 40) {
                extracto = candidato;
                break;
            }
        }
        return new String[]{descripcion, extracto};
    }

    /** Número de etiquetas SPORT_VALUE distintas. */
    static int numeroDeportes(List<String> etiquetas) {
        return (int) etiquetas.stream().filter(e -> e.startsWith("SPORT_VALUE:")).distinct().count();
    }

    // ---------------- utilidades ----------------

    /** Nombre, tipo y todas las etiquetas: para comprobar el deporte. */
    private static String textoDeporte(JsonObject p) {
        StringBuilder datos = new StringBuilder(texto(p, "title")).append(' ').append(texto(p, "type"));
        for (String etiqueta : etiquetas(p)) {
            datos.append(' ').append(etiqueta);
        }
        return datos.toString();
    }

    /**
     * Tipo y naturaleza: para comprobar si es calzado, equipo... No se usa el
     * subdepartamento porque es demasiado general ("SHOES & APPAREL" incluye camisetas).
     */
    private static String textoTipo(JsonObject p) {
        StringBuilder datos = new StringBuilder(texto(p, "type"));
        for (String etiqueta : etiquetas(p)) {
            if (etiqueta.startsWith("NATURE_VALUE:")) {
                datos.append(' ').append(etiqueta.substring("NATURE_VALUE:".length()));
            }
        }
        return datos.toString();
    }

    /** true si el texto contiene alguna de las palabras o expresiones completas (o si no hay). */
    static boolean contienePalabraCompleta(String texto, List<String> palabras) {
        if (palabras.isEmpty()) {
            return true;
        }
        String normalizado = " " + normalizar(texto).replaceAll("[^a-z0-9]+", " ").trim() + " ";
        for (String palabra : palabras) {
            String buscada = " " + normalizar(palabra).replaceAll("[^a-z0-9]+", " ").trim() + " ";
            if (normalizado.contains(buscada)) {
                return true;
            }
        }
        return false;
    }

    static List<String> etiquetas(JsonObject p) {
        List<String> lista = new ArrayList<>();
        JsonElement tags = p.get("tags");
        if (tags != null && tags.isJsonArray()) {
            for (JsonElement t : tags.getAsJsonArray()) {
                lista.add(t.getAsString());
            }
        } else if (tags != null && tags.isJsonPrimitive()) {
            for (String t : tags.getAsString().split(",")) {
                lista.add(t.trim());
            }
        }
        return lista;
    }

    /** true si el texto contiene alguna de las palabras (o si no hay palabras). */
    private static boolean contieneAlguna(String texto, List<String> palabras) {
        if (palabras.isEmpty()) {
            return true;
        }
        String normalizado = normalizar(texto);
        for (String palabra : palabras) {
            if (normalizado.contains(normalizar(palabra))) {
                return true;
            }
        }
        return false;
    }

    /** Textos de los párrafos HTML, sin etiquetas y sin vacíos. */
    static List<String> parrafos(String html) {
        List<String> lista = new ArrayList<>();
        Matcher m = PARRAFO.matcher(html);
        while (m.find()) {
            String limpio = decodificar(m.group(1).replaceAll("<[^>]+>", " "))
                    .replaceAll("\\s+", " ").trim();
            if (!limpio.isEmpty()) {
                lista.add(limpio);
            }
        }
        return lista;
    }

    static String formatearPrecio(String precio, String moneda) {
        try {
            double valor = Double.parseDouble(precio);
            return String.format(Locale.forLanguageTag("es-ES"), "%,.2f %s", valor, moneda);
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private static String decodificar(String s) {
        return s.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
                .replace("&#39;", "'").replace("&nbsp;", " ").replace("&amp;", "&");
    }

    static String texto(JsonObject obj, String campo) {
        JsonElement e = obj.get(campo);
        return (e == null || e.isJsonNull() || !e.isJsonPrimitive()) ? "" : e.getAsString();
    }

    static String normalizar(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private static String codificar(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String vacioANull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}
