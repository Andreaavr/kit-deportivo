/**
 * Familias de productos: un subpaquete por deporte.
 *
 * <p>Cada persona añade aquí sus dos deportes, en su propia rama:</p>
 * <ul>
 *   <li>Persona A: {@code familias.fitness} y {@code familias.running}</li>
 *   <li>Persona B: {@code familias.natacion} y {@code familias.gimnasia}</li>
 * </ul>
 *
 * <p>Cada subpaquete contiene cuatro clases:</p>
 * <ul>
 *   <li>{@code Fabrica<Deporte>}: implementa {@code FabricaDeportiva}, recibe una
 *       {@code FuenteDatosMaterial} en el constructor y crea las tres listas
 *       (equipos, calzados y accesorios), cada una con un {@code Criterio}
 *       (deporte y palabras completas del tipo).</li>
 *   <li>{@code Equipo<Deporte>}, {@code Calzado<Deporte>} y {@code Accesorio<Deporte>}:
 *       implementan su interfaz, guardan su {@code InfoMaterial} y no son públicas.</li>
 * </ul>
 *
 * <p>Después, el deporte se registra en {@code Main}, en la zona de cada persona.</p>
 */
package deportes.familias;
