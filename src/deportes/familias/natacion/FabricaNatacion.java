package deportes.familias.natacion;

import deportes.datos.Criterio;
import deportes.datos.FuenteDatosMaterial;
import deportes.fabrica.FabricaDeportiva;
import deportes.productos.AccesorioDeportivo;
import deportes.productos.CalzadoDeportivo;
import deportes.productos.EquipoPrincipal;

import java.util.List;

/**
 * Fábrica concreta de la familia Natación.
 * Decide QUÉ productos forman cada categoría: de qué deporte tienen que ser y
 * de qué tipo. Los datos los aporta la fuente; la fábrica solo los envuelve en
 * sus productos concretos.
 */
public class FabricaNatacion implements FabricaDeportiva {

    /** Etiqueta de deporte que usa la tienda para natación. */
    private static final String[] DEPORTE = {"swimming"};

    private final FuenteDatosMaterial fuente;

    public FabricaNatacion(FuenteDatosMaterial fuente) {
        this.fuente = fuente;
    }

    @Override
    public List<EquipoPrincipal> crearEquipos() {
        return fuente.buscar(Criterio.de("swimming goggles").deporte(DEPORTE).tipo(
                        "swimming goggles", "swim cap", "swimming mask", "center snorkel",
                        "flippers", "kick board", "pull buoy", "swimming paddle", "swim gloves"))
                .stream().<EquipoPrincipal>map(EquipoNatacion::new).toList();
    }

    @Override
    public List<CalzadoDeportivo> crearCalzados() {
        return fuente.buscar(Criterio.de("pool sandals").deporte(DEPORTE).tipo(Criterio.CALZADO))
                .stream().<CalzadoDeportivo>map(CalzadoNatacion::new).toList();
    }

    @Override
    public List<AccesorioDeportivo> crearAccesorios() {
        return fuente.buscar(Criterio.de("swimming towel").deporte(DEPORTE).tipo(
                        "towel", "nose clip", "earplug", "mesh bag", "pouch", "watertight pocket",
                        "glasses case", "anti fogging", "changing robe", "bathrobe"))
                .stream().<AccesorioDeportivo>map(AccesorioNatacion::new).toList();
    }
}
