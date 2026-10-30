package deportes.productos;

/**
 * Producto abstracto: calzado específico de una disciplina deportiva.
 */
public interface CalzadoDeportivo extends ProductoDeportivo {

    @Override
    default String getTipo() {
        return "Calzado";
    }
}
