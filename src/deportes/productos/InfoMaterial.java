package deportes.productos;

/**
 * Datos reales de un producto, obtenidos de la tienda.
 *
 * @param titulo      nombre del producto
 * @param marca       marca (puede ser null)
 * @param descripcion descripción corta
 * @param extracto    texto descriptivo adicional (puede ser null)
 * @param precio      precio ya formateado con su moneda, p. ej. "349,00 QAR" (puede ser null)
 * @param urlImagen   foto del producto (puede ser null)
 * @param urlEnlace   página del producto en la tienda (null si no se encontró)
 */
public record InfoMaterial(String titulo, String marca, String descripcion, String extracto,
                           String precio, String urlImagen, String urlEnlace) {

    /** La fuente respondió, pero no tiene un producto que encaje. */
    public static InfoMaterial noEncontrado(String material) {
        return aviso(material, "No se ha encontrado este producto.");
    }

    /** No se pudo contactar con la tienda (sin conexión, tiempo agotado...). */
    public static InfoMaterial sinConexion(String material) {
        return aviso(material, "Sin conexión: no se pudo consultar la tienda.");
    }

    /** Producto sin datos, con un mensaje que explica el motivo. */
    public static InfoMaterial aviso(String material, String mensaje) {
        return new InfoMaterial(material, null, mensaje, null, null, null, null);
    }

    public boolean encontrado() {
        return urlEnlace != null;
    }
}
