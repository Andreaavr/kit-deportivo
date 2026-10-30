package deportes.datos;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Traductor que usa la API pública de MyMemory (sin clave).
 * Límite gratuito: 5.000 caracteres al día, o 50.000 indicando un correo
 * (variable de entorno {@code MYMEMORY_EMAIL}). Al llegar al límite deja
 * de llamar a la API hasta el día siguiente y los textos quedan en inglés.
 */
public class TraductorMyMemory implements Traductor {

    private static final String URL = "https://api.mymemory.translated.net/get";
    /** MyMemory admite textos cortos por petición. */
    static final int MAX_CARACTERES = 450;

    private final String correo;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private volatile boolean limiteAlcanzado;

    /** @param correo correo para ampliar el límite diario, o null */
    public TraductorMyMemory(String correo) {
        this.correo = (correo == null || correo.isBlank()) ? null : correo.trim();
    }

    @Override
    public String traducir(String textoIngles) {
        if (limiteAlcanzado || textoIngles == null || textoIngles.isBlank()) {
            return null;
        }
        String texto = recortar(textoIngles.trim());
        String url = URL + "?q=" + codificar(texto) + "&langpair=en%7Ces"
                + (correo == null ? "" : "&de=" + codificar(correo));
        try {
            HttpRequest peticion = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", ClienteShopify.USER_AGENT)
                    .GET()
                    .build();
            HttpResponse<String> respuesta =
                    http.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (respuesta.statusCode() == 429) {
                limiteAlcanzado = true;
                return null;
            }
            if (respuesta.statusCode() != 200) {
                return null;
            }
            Respuesta r = leerRespuesta(respuesta.body());
            if (r.limiteAlcanzado()) {
                limiteAlcanzado = true;
            }
            return r.traduccion();
        } catch (IOException | RuntimeException e) {
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /** Resultado de interpretar la respuesta de MyMemory. */
    record Respuesta(String traduccion, boolean limiteAlcanzado) {
    }

    /** Interpreta el JSON de MyMemory: {@code responseStatus} y {@code responseData.translatedText}. */
    static Respuesta leerRespuesta(String json) {
        JsonObject raiz = JsonParser.parseString(json).getAsJsonObject();
        JsonElement estado = raiz.get("responseStatus");
        int codigo = 0;
        if (estado != null && !estado.isJsonNull()) {
            try {
                codigo = Integer.parseInt(estado.getAsString().trim());
            } catch (NumberFormatException ignorado) {
                codigo = 0;
            }
        }
        String texto = null;
        JsonObject datos = raiz.getAsJsonObject("responseData");
        if (datos != null && datos.get("translatedText") != null && !datos.get("translatedText").isJsonNull()) {
            texto = datos.get("translatedText").getAsString();
        }
        boolean aviso = texto != null && texto.toUpperCase().contains("MYMEMORY WARNING");
        if (codigo != 200 || texto == null || texto.isBlank() || aviso) {
            return new Respuesta(null, aviso || codigo == 429);
        }
        return new Respuesta(decodificar(texto), false);
    }

    static String recortar(String texto) {
        if (texto.length() <= MAX_CARACTERES) {
            return texto;
        }
        int corte = texto.lastIndexOf(' ', MAX_CARACTERES);
        return texto.substring(0, corte > 0 ? corte : MAX_CARACTERES);
    }

    private static String decodificar(String s) {
        return s.replace("&#39;", "'").replace("&quot;", "\"").replace("&lt;", "<")
                .replace("&gt;", ">").replace("&amp;", "&");
    }

    private static String codificar(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
