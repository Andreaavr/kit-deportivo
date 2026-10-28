package deportes.cliente;

import deportes.fabrica.FabricaDeportiva;
import deportes.productos.AccesorioDeportivo;
import deportes.productos.CalzadoDeportivo;
import deportes.productos.EquipoPrincipal;

/**
 * Cliente del patrón. Solo depende de interfaces: nunca conoce
 * qué deporte concreto está preparando.
 */
public class GestorKitDeportivo {
    private final EquipoPrincipal equipo;
    private final CalzadoDeportivo calzado;
    private final AccesorioDeportivo accesorio;

    public GestorKitDeportivo(FabricaDeportiva fabrica) {
        this.equipo = fabrica.crearEquipo();
        this.calzado = fabrica.crearCalzado();
        this.accesorio = fabrica.crearAccesorio();
    }

    public void prepararSesion() {
        System.out.println("\n--- Preparando tu equipamiento deportivo ---");
        equipo.preparar();
        calzado.calzar();
        accesorio.usar();
        System.out.println("¡Todo listo para tu entrenamiento sin incompatibilidades!\n");
    }
}
