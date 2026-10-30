package deportes.datos;

/**
 * Traduce textos del inglés (idioma de la tienda) al español.
 * Es una estrategia: se puede cambiar de servicio sin tocar el resto.
 */
public interface Traductor {

    /** @return el texto en español, o null si no se pudo traducir */
    String traducir(String textoIngles);
}
