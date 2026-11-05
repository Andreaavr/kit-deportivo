package deportes.familias.running;

import deportes.productos.AccesorioDeportivo;
import deportes.productos.InfoMaterial;

/** Accesorio (cinturones, botellas, gorras…) de running. Solo la crea {@link FabricaRunning}. */
class AccesorioRunning implements AccesorioDeportivo {

    private final InfoMaterial info;

    AccesorioRunning(InfoMaterial info) {
        this.info = info;
    }

    @Override
    public InfoMaterial getInfo() {
        return info;
    }
}
