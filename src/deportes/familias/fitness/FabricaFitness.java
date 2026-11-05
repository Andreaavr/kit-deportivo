package deportes.familias.fitness;

import deportes.datos.Criterio;
import deportes.datos.FuenteDatosMaterial;
import deportes.fabrica.FabricaDeportiva;
import deportes.productos.AccesorioDeportivo;
import deportes.productos.CalzadoDeportivo;
import deportes.productos.EquipoPrincipal;

import java.util.List;

/**
 * Fábrica concreta de la familia Fitness.
 * Decide QUÉ productos forman cada categoría: de qué deporte tienen que ser y
 * de qué tipo. Los datos los aporta la fuente; la fábrica solo los envuelve en
 * sus productos concretos.
 * <p>"dumbell" está escrito así a propósito: es como lo etiqueta la tienda.</p>
 */
public class FabricaFitness implements FabricaDeportiva {

    /** Deporte: las etiquetas que usa la tienda para fitness y musculación. */
    private static final String[] DEPORTE = {"fitness", "body building", "cross training"};

    private final FuenteDatosMaterial fuente;

    public FabricaFitness(FuenteDatosMaterial fuente) {
        this.fuente = fuente;
    }

    @Override
    public List<EquipoPrincipal> crearEquipos() {
        return fuente.buscar(Criterio.de("dumbbell").deporte(DEPORTE).tipo(
                        "dumbell", "kettlebell", "weight plate", "weight set", "bar",
                        "weightlifting bar", "weightlifting bumper disc", "press bench", "squat rack",
                        "treadmill", "exercise bike", "rowing machine", "cross trainer", "stepper",
                        "home gym", "plyo box", "dips station", "step"))
                .stream().<EquipoPrincipal>map(EquipoFitness::new).toList();
    }

    @Override
    public List<CalzadoDeportivo> crearCalzados() {
        return fuente.buscar(Criterio.de("training shoes").deporte(DEPORTE).tipo(Criterio.CALZADO))
                .stream().<CalzadoDeportivo>map(CalzadoFitness::new).toList();
    }

    @Override
    public List<AccesorioDeportivo> crearAccesorios() {
        return fuente.buscar(Criterio.de("resistance band").deporte(DEPORTE).tipo(
                        "skipping rope", "elastic band", "resistance band", "glute band", "handgrip",
                        "gloves", "lifting strap", "lumbar belt", "wrist support", "sports bag",
                        "water bottle", "shaker", "fitness mat", "abdominal wheel", "suspension strap",
                        "towel", "knee pad"))
                .stream().<AccesorioDeportivo>map(AccesorioFitness::new).toList();
    }
}
