package deportes.productos;

/**
 * Producto abstracto: equipo principal de una disciplina deportiva.
 */
public interface EquipoPrincipal extends ProductoDeportivo {

    @Override
    default String getTipo() {
        return "Equipo principal";
    }
}
