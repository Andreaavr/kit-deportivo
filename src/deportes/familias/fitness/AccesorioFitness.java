package deportes.familias.fitness;

import deportes.productos.AccesorioDeportivo;
import deportes.productos.InfoMaterial;

/** Accesorio (bandas, guantes, cinturones…) de fitness. Solo la crea {@link FabricaFitness}. */
class AccesorioFitness implements AccesorioDeportivo {

    private final InfoMaterial info;

    AccesorioFitness(InfoMaterial info) {
        this.info = info;
    }

    @Override
    public InfoMaterial getInfo() {
        return info;
    }
}
