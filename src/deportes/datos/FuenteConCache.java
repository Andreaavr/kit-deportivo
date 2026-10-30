package deportes.datos;

import deportes.productos.InfoMaterial;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decorador de una fuente de datos: guarda en memoria los resultados
 * ya obtenidos para no repetir consultas ni traducciones al volver a un deporte.
 *
 * Envuelve a cualquier {@link FuenteDatosMaterial} sin modificarla.
 * Solo guarda los resultados encontrados; si no hubo datos, se vuelve
 * a intentar la próxima vez.
 */
public class FuenteConCache implements FuenteDatosMaterial {

    private final FuenteDatosMaterial fuente;
    private final Map<Criterio, List<InfoMaterial>> cache = new ConcurrentHashMap<>();

    public FuenteConCache(FuenteDatosMaterial fuente) {
        this.fuente = Objects.requireNonNull(fuente, "fuente");
    }

    @Override
    public List<InfoMaterial> buscar(Criterio criterio) {
        List<InfoMaterial> guardado = cache.get(criterio);
        if (guardado != null) {
            return guardado;
        }
        List<InfoMaterial> nuevo = List.copyOf(fuente.buscar(criterio));
        if (!nuevo.isEmpty() && nuevo.get(0).encontrado()) {
            cache.put(criterio, nuevo);
        }
        return nuevo;
    }
}
