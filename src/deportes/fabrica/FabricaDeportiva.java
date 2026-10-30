package deportes.fabrica;

import deportes.productos.AccesorioDeportivo;
import deportes.productos.CalzadoDeportivo;
import deportes.productos.EquipoPrincipal;

import java.util.List;

/**
 * Fábrica abstracta: declara un método de creación por cada tipo de producto
 * de la familia. Cada deporte la implementa con su propia fábrica concreta,
 * que garantiza que todo lo que crea es de ese deporte.
 *
 * Cada método devuelve todos los productos de ese tipo y nunca una lista
 * vacía: si no hay ninguno, devuelve un único producto que lo indica.
 */
public interface FabricaDeportiva {
    List<EquipoPrincipal> crearEquipos();
    List<CalzadoDeportivo> crearCalzados();
    List<AccesorioDeportivo> crearAccesorios();
}