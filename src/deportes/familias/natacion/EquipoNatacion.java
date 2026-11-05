package deportes.familias.natacion;

import deportes.productos.EquipoPrincipal;
import deportes.productos.InfoMaterial;

/** Equipo principal (gafas, gorros, aletas…) de natación. Solo la crea {@link FabricaNatacion}. */
class EquipoNatacion implements EquipoPrincipal {

    private final InfoMaterial info;

    EquipoNatacion(InfoMaterial info) {
        this.info = info;
    }

    @Override
    public InfoMaterial getInfo() {
        return info;
    }
}
