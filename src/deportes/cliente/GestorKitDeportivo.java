package deportes.cliente;

import deportes.fabrica.FabricaDeportiva;
import deportes.productos.AccesorioDeportivo;
import deportes.productos.CalzadoDeportivo;
import deportes.productos.EquipoPrincipal;

import java.util.List;

/**
 * Cliente del patrón. Solo depende de interfaces: nunca conoce
 * qué deporte concreto está preparando ni de dónde salen los datos.
 * Pide a la fábrica todos los productos de cada tipo.
 */
public class GestorKitDeportivo {

    private final List<EquipoPrincipal> equipos;
    private final List<CalzadoDeportivo> calzados;
    private final List<AccesorioDeportivo> accesorios;

    public GestorKitDeportivo(FabricaDeportiva fabrica) {
        this.equipos = List.copyOf(fabrica.crearEquipos());
        this.calzados = List.copyOf(fabrica.crearCalzados());
        this.accesorios = List.copyOf(fabrica.crearAccesorios());
    }

    public List<EquipoPrincipal> getEquipos() {
        return equipos;
    }

    public List<CalzadoDeportivo> getCalzados() {
        return calzados;
    }

    public List<AccesorioDeportivo> getAccesorios() {
        return accesorios;
    }
}