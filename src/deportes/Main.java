package deportes;

import deportes.cliente.GestorKitDeportivo;
import deportes.fabrica.FabricaDeportiva;

// --- Imports Persona A (fitness y running) ---

// --- Imports Persona B (escalada y gimnasia) ---

import java.util.Scanner;

/**
 * Punto de entrada. Es el único lugar del programa que conoce
 * las fábricas concretas: elige una y se la entrega al gestor.
 */
public class Main {

    public static void main(String[] args) {
        String deporte = (args.length > 0) ? args[0] : pedirDeporte();

        FabricaDeportiva fabrica;
        try {
            fabrica = crearFabrica(deporte);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return;
        }

        GestorKitDeportivo gestor = new GestorKitDeportivo(fabrica);
        gestor.prepararSesion();
    }

    private static FabricaDeportiva crearFabrica(String deporte) {
        switch (deporte.trim().toUpperCase()) {
            // --- Casos Persona A (fitness y running) ---

            // --- Casos Persona B (escalada y gimnasia) ---

            default:
                throw new IllegalArgumentException("Deporte no disponible: " + deporte);
        }
    }

    private static String pedirDeporte() {
        System.out.println("Elige un deporte:");
        System.out.println("  1. Fitness");
        System.out.println("  2. Running");
        System.out.println("  3. Escalada");
        System.out.println("  4. Gimnasia");
        System.out.print("> ");
        Scanner sc = new Scanner(System.in);
        return sc.hasNextLine() ? sc.nextLine() : "";
    }
}
