package deportes.productos;

/**
 * Comportamiento común a todos los productos del kit:
 * saben qué tipo de producto son y qué datos reales tienen.
 *
 * Los productos concretos implementan una de las tres interfaces
 * (EquipoPrincipal, CalzadoDeportivo o AccesorioDeportivo), que ya
 * aportan getTipo(); solo tienen que guardar su InfoMaterial.
 */
public interface ProductoDeportivo {

    /** Datos reales del material (nombre, descripción, imagen, enlace...). */
    InfoMaterial getInfo();

    /** Tipo de producto dentro del kit, para mostrarlo en la ventana. */
    String getTipo();
}
