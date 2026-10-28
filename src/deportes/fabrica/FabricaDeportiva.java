package deportes.fabrica;

import deportes.productos.AccesorioDeportivo;
import deportes.productos.CalzadoDeportivo;
import deportes.productos.EquipoPrincipal;

/**
 * Fábrica abstracta: declara un método de creación por cada producto
 * de la familia. Cada deporte la implementa con su propia fábrica concreta.
 */
public interface FabricaDeportiva {
    EquipoPrincipal crearEquipo();
    CalzadoDeportivo crearCalzado();
    AccesorioDeportivo crearAccesorio();
}
