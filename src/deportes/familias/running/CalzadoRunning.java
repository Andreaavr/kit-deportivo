package deportes.familias.running;

import deportes.productos.CalzadoDeportivo;
import deportes.productos.InfoMaterial;

/** Zapatillas de running de running. Solo la crea {@link FabricaRunning}. */
class CalzadoRunning implements CalzadoDeportivo {

    private final InfoMaterial info;

    CalzadoRunning(InfoMaterial info) {
        this.info = info;
    }

    @Override
    public InfoMaterial getInfo() {
        return info;
    }
}
