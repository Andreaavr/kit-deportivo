package deportes.familias.fitness;

import deportes.productos.EquipoPrincipal;
import deportes.productos.InfoMaterial;

/** Equipo principal (mancuernas, bancos, máquinas…) de fitness. Solo la crea {@link FabricaFitness}. */
class EquipoFitness implements EquipoPrincipal {

    private final InfoMaterial info;

    EquipoFitness(InfoMaterial info) {
        this.info = info;
    }

    @Override
    public InfoMaterial getInfo() {
        return info;
    }
}
