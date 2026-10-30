package deportes.datos;

import java.util.List;

/**
 * Qué productos se quieren obtener:
 * <ul>
 *   <li>{@code busqueda}: palabras que ordenan los resultados (primero los que
 *       más tienen en el nombre). Están en inglés, como la tienda.</li>
 *   <li>{@code deporte}: palabras que deben aparecer en los deportes del
 *       producto (p. ej. "swimming").</li>
 *   <li>{@code tipo}: palabras o expresiones completas que deben aparecer en el
 *       tipo del producto (p. ej. "swimming goggles", "shoes").</li>
 * </ul>
 * Se construye así:
 * <pre>
 *     Criterio.de("goggles").deporte("swimming").tipo("swimming goggles", "swim cap")
 * </pre>
 */
public record Criterio(String busqueda, List<String> deporte, List<String> tipo) {

    /**
     * Palabras completas que identifican cualquier calzado, comunes a todos los
     * deportes. Son palabras completas: "shoes" encaja con "Sport shoes", pero
     * "Shoe bag" (una bolsa) o "Shoelaces" (cordones) no.
     */
    public static final String[] CALZADO = {"shoes", "boots", "sandals", "slippers", "trainers",
            "booties", "sneakers", "footwear", "aquashoes", "flip flops", "clogs"};

    public Criterio {
        deporte = List.copyOf(deporte);
        tipo = List.copyOf(tipo);
    }

    /** Empieza un criterio con las palabras que ordenan los resultados. */
    public static Criterio de(String busqueda) {
        return new Criterio(busqueda, List.of(), List.of());
    }

    /** Palabras del deporte (basta con que aparezca una). */
    public Criterio deporte(String... palabras) {
        return new Criterio(busqueda, List.of(palabras), tipo);
    }

    /** Palabras completas del tipo de producto (basta con que aparezca una). */
    public Criterio tipo(String... palabras) {
        return new Criterio(busqueda, deporte, List.of(palabras));
    }
}
