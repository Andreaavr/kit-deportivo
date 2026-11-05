package deportes.familias.gimnasia;

import deportes.productos.EquipoPrincipal;
import deportes.productos.InfoMaterial;

/** Equipo principal (esterillas, tablas de equilibrio, camas elásticas…) de gimnasia. Solo la crea {@link FabricaGimnasia}. */
class EquipoGimnasia implements EquipoPrincipal {

    private final InfoMaterial info;

    EquipoGimnasia(InfoMaterial info) {
        this.info = info;
    }

    @Override
    public InfoMaterial getInfo() {
        return info;
    }
}
