package deportes;

import deportes.datos.ClienteBaseDatos;
import deportes.datos.FuenteConCache;
import deportes.datos.FuenteDatosMaterial;
import deportes.datos.FuenteTraducida;
import deportes.datos.Traductor;
import deportes.datos.TraductorConMemoria;
import deportes.datos.TraductorMyMemory;
import deportes.fabrica.FabricaDeportiva;
import deportes.vista.VentanaKit;

// --- Imports Persona A (fitness y running) ---

// --- Imports Persona B (natación y gimnasia) ---

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Punto de entrada. Es el único lugar del programa que conoce las clases
 * concretas: monta la fuente de datos, registra las fábricas de cada deporte
 * y abre la ventana.
 */
public class Main {

    public static void main(String[] args) {
        Path archivo = Path.of("datos", "catalogo.db");

        // Fuente de datos: la base de datos local (creada con ImportadorCatalogo),
        // traducida al español y con caché. Cada capa es un decorador:
        //   caché -> traducción -> base de datos
        // Alternativa en vivo, sin base de datos:
        //   new ClienteShopify("https://decathlon.qa", "QAR") en lugar de baseDatos
        ClienteBaseDatos baseDatos = new ClienteBaseDatos(archivo);
        Traductor traductor = new TraductorConMemoria(
                new TraductorMyMemory(System.getenv("MYMEMORY_EMAIL")), archivo);
        FuenteDatosMaterial fuente = new FuenteConCache(new FuenteTraducida(baseDatos, traductor));

        // Catálogo de deportes: nombre visible -> cómo crear su fábrica
        Map<String, Supplier<FabricaDeportiva>> deportes = new LinkedHashMap<>();

        // --- Deportes Persona A (fitness y running) ---

        // --- Deportes Persona B (natación y gimnasia) ---

        VentanaKit.abrir(deportes, List.of(
                baseDatos.describirOrigen(),
                baseDatos.describirPrecios(),
                "Rates By Exchange Rate API",
                "Traducción automática: MyMemory"));
    }
}