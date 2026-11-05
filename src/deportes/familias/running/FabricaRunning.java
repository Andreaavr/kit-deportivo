package deportes.familias.running;

import deportes.datos.Criterio;
import deportes.datos.FuenteDatosMaterial;
import deportes.fabrica.FabricaDeportiva;
import deportes.productos.AccesorioDeportivo;
import deportes.productos.CalzadoDeportivo;
import deportes.productos.EquipoPrincipal;

import java.util.List;

/**
 * Fábrica concreta de la familia Running.
 * Decide QUÉ productos forman cada categoría: de qué deporte tienen que ser y
 * de qué tipo. Los datos los aporta la fuente; la fábrica solo los envuelve en
 * sus productos concretos.
 */
public class FabricaRunning implements FabricaDeportiva {

    /** Deporte: las etiquetas que usa la tienda para running. */
    private static final String[] DEPORTE = {"jogging", "road running"};

    private final FuenteDatosMaterial fuente;

    public FabricaRunning(FuenteDatosMaterial fuente) {
        this.fuente = fuente;
    }

    @Override
    public List<EquipoPrincipal> crearEquipos() {
        return fuente.buscar(Criterio.de("gps watch").deporte(DEPORTE).tipo(
                        "watch", "fitness monitor", "hydration backpack", "running vest", "headlamp"))
                .stream().<EquipoPrincipal>map(EquipoRunning::new).toList();
    }

    @Override
    public List<CalzadoDeportivo> crearCalzados() {
        return fuente.buscar(Criterio.de("running shoes").deporte(DEPORTE).tipo(Criterio.CALZADO))
                .stream().<CalzadoDeportivo>map(CalzadoRunning::new).toList();
    }

    @Override
    public List<AccesorioDeportivo> crearAccesorios() {
        return fuente.buscar(Criterio.de("running belt").deporte(DEPORTE).tipo(
                        "belt", "flask", "water bottle", "bottle", "arm guard", "smartphone holder",
                        "insoles", "massage roller", "massage gun", "calf compression sleeves", "gloves",
                        "headband", "cap", "visor"))
                .stream().<AccesorioDeportivo>map(AccesorioRunning::new).toList();
    }
}
