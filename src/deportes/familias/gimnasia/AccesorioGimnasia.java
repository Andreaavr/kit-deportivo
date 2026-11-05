package deportes.familias.gimnasia;

import deportes.productos.AccesorioDeportivo;
import deportes.productos.InfoMaterial;

/** Accesorio (aros, pelotas, cintas…) de gimnasia. Solo la crea {@link FabricaGimnasia}. */
class AccesorioGimnasia implements AccesorioDeportivo {

    private final InfoMaterial info;

    AccesorioGimnasia(InfoMaterial info) {
        this.info = info;
    }

    @Override
    public InfoMaterial getInfo() {
        return info;
    }
}
