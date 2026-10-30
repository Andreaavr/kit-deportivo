package deportes.datos;

import deportes.productos.InfoMaterial;

import java.util.List;

/**
 * Abstracción de la fuente de datos de los productos (patrón Strategy):
 * define cómo se obtienen los datos, y cada implementación es una estrategia
 * intercambiable (la base de datos local, la tienda en vivo...). Los decoradores
 * (caché, traducción) también la implementan.
 *
 * Las fábricas dependen de esta interfaz, no de una fuente concreta
 * (principio de inversión de dependencias).
 */
public interface FuenteDatosMaterial {

    /**
     * Obtiene todos los productos que cumplen el criterio (son del deporte y
     * del tipo indicados), ordenados por cuántas palabras de la búsqueda
     * tienen en el nombre.
     *
     * @return al menos un elemento; si no hay productos, uno solo sin datos que
     *         explica el motivo ({@code encontrado() == false})
     */
    List<InfoMaterial> buscar(Criterio criterio);
}
