package deportes.familias.running;

import deportes.productos.EquipoPrincipal;
import deportes.productos.InfoMaterial;

/** Equipo principal (relojes, mochilas de hidratación, frontales…) de running. Solo la crea {@link FabricaRunning}. */
class EquipoRunning implements EquipoPrincipal {

    private final InfoMaterial info;

    EquipoRunning(InfoMaterial info) {
        this.info = info;
    }

    @Override
    public InfoMaterial getInfo() {
        return info;
    }
}
