package deportes.familias.natacion;

import deportes.productos.AccesorioDeportivo;
import deportes.productos.InfoMaterial;

/** Accesorio (toallas, pinzas nasales…) de natación. Solo la crea {@link FabricaNatacion}. */
class AccesorioNatacion implements AccesorioDeportivo {

    private final InfoMaterial info;

    AccesorioNatacion(InfoMaterial info) {
        this.info = info;
    }

    @Override
    public InfoMaterial getInfo() {
        return info;
    }
}
