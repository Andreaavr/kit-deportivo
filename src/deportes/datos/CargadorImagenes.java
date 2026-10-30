package deportes.datos;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Descarga las fotos de los productos identificando la aplicación con un
 * User-Agent. Guarda en memoria las ya descargadas para que volver a un
 * deporte sea instantáneo.
 */
public final class CargadorImagenes {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final Map<String, BufferedImage> CACHE = new ConcurrentHashMap<>();

    private CargadorImagenes() {
    }

    /** @return la imagen, o null si no hay URL o no se puede descargar. */
    public static BufferedImage cargar(String url) {
        if (url == null) {
            return null;
        }
        BufferedImage guardada = CACHE.get(url);
        if (guardada != null) {
            return guardada;
        }
        BufferedImage imagen = descargar(reducida(url));
        if (imagen != null) {
            CACHE.put(url, imagen);
        }
        return imagen;
    }

    /** Las fotos de Shopify admiten "width": se pide una versión pequeña, mucho más rápida. */
    static String reducida(String url) {
        if (url.contains("cdn.shopify.com") && !url.contains("width=")) {
            return url + (url.contains("?") ? "&" : "?") + "width=400";
        }
        return url;
    }

    private static BufferedImage descargar(String url) {
        try {
            HttpRequest peticion = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", ClienteShopify.USER_AGENT)
                    .GET()
                    .build();
            HttpResponse<byte[]> respuesta =
                    HTTP.send(peticion, HttpResponse.BodyHandlers.ofByteArray());
            if (respuesta.statusCode() != 200) {
                return null;
            }
            return ImageIO.read(new ByteArrayInputStream(respuesta.body()));
        } catch (IOException | IllegalArgumentException e) {
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
