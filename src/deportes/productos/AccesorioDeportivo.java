package deportes.productos;

/**
 * Producto abstracto: accesorio complementario de una disciplina deportiva.
 */
public interface AccesorioDeportivo extends ProductoDeportivo {

    @Override
    default String getTipo() {
        return "Accesorio";
    }
}