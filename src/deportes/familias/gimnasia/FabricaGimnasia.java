package deportes.familias.gimnasia;

import deportes.datos.Criterio;
import deportes.datos.FuenteDatosMaterial;
import deportes.fabrica.FabricaDeportiva;
import deportes.productos.AccesorioDeportivo;
import deportes.productos.CalzadoDeportivo;
import deportes.productos.EquipoPrincipal;

import java.util.List;

/**
 * Fábrica concreta de la familia Gimnasia.
 * Decide QUÉ productos forman cada categoría: de qué deporte tienen que ser y
 * de qué tipo. Los datos los aporta la fuente; la fábrica solo los envuelve en
 * sus productos concretos.
 */
public class FabricaGimnasia implements FabricaDeportiva {

    /** Deporte: la etiqueta que usa la tienda para gimnasia. */
    private static final String[] DEPORTE = {"gymnastics"};

    private final FuenteDatosMaterial fuente;

    public FabricaGimnasia(FuenteDatosMaterial fuente) {
        this.fuente = fuente;
    }

    @Override
    public List<EquipoPrincipal> crearEquipos() {
        return fuente.buscar(Criterio.de("gymnastics mat").deporte(DEPORTE).tipo(
                        "fitness mat", "pilates mat", "balance board", "balance disc", "trampoline",
                        "beam", "foam pad"))
                .stream().<EquipoPrincipal>map(EquipoGimnasia::new).toList();
    }

    @Override
    public List<CalzadoDeportivo> crearCalzados() {
        return fuente.buscar(Criterio.de("gymnastics slippers").deporte(DEPORTE).tipo(Criterio.CALZADO))
                .stream().<CalzadoDeportivo>map(CalzadoGimnasia::new).toList();
    }

    @Override
    public List<AccesorioDeportivo> crearAccesorios() {
        return fuente.buscar(Criterio.de("gymnastics hoop").deporte(DEPORTE).tipo(
                        "hoop", "jumping ball", "swiss ball", "gymnastic ribbon", "rg club",
                        "skipping rope", "elastic band", "toning handle", "medicine ball", "soft ball",
                        "weighted ball", "mat bag"))
                .stream().<AccesorioDeportivo>map(AccesorioGimnasia::new).toList();
    }
}
