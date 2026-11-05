package deportes.familias.gimnasia;

import deportes.productos.CalzadoDeportivo;
import deportes.productos.InfoMaterial;

/** Calzado (medias puntas y zapatillas de gimnasia) de gimnasia. Solo la crea {@link FabricaGimnasia}. */
class CalzadoGimnasia implements CalzadoDeportivo {

    private final InfoMaterial info;

    CalzadoGimnasia(InfoMaterial info) {
        this.info = info;
    }

    @Override
    public InfoMaterial getInfo() {
        return info;
    }
}
