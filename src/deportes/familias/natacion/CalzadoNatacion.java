package deportes.familias.natacion;

import deportes.productos.CalzadoDeportivo;
import deportes.productos.InfoMaterial;

/** Calzado (chanclas y escarpines) de natación. Solo la crea {@link FabricaNatacion}. */
class CalzadoNatacion implements CalzadoDeportivo {

    private final InfoMaterial info;

    CalzadoNatacion(InfoMaterial info) {
        this.info = info;
    }

    @Override
    public InfoMaterial getInfo() {
        return info;
    }
}
