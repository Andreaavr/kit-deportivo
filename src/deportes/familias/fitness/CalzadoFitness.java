package deportes.familias.fitness;

import deportes.productos.CalzadoDeportivo;
import deportes.productos.InfoMaterial;

/** Calzado de entrenamiento de fitness. Solo la crea {@link FabricaFitness}. */
class CalzadoFitness implements CalzadoDeportivo {

    private final InfoMaterial info;

    CalzadoFitness(InfoMaterial info) {
        this.info = info;
    }

    @Override
    public InfoMaterial getInfo() {
        return info;
    }
}
