package deportes.datos;

import deportes.productos.InfoMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Decorador de una fuente de datos: devuelve los mismos productos con el
 * nombre y la descripción traducidos al español. Si un texto no se puede
 * traducir (sin conexión o límite diario agotado), se queda en inglés.
 */
public class FuenteTraducida implements FuenteDatosMaterial {

    /** Longitud máxima de la descripción que se traduce y se muestra. */
    static final int MAX_DESCRIPCION = 300;

    private final FuenteDatosMaterial fuente;
    private final Traductor traductor;
    private final ExecutorService hilos = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "traduccion");
        t.setDaemon(true);
        return t;
    });

    public FuenteTraducida(FuenteDatosMaterial fuente, Traductor traductor) {
        this.fuente = Objects.requireNonNull(fuente, "fuente");
        this.traductor = Objects.requireNonNull(traductor, "traductor");
    }

    @Override
    public List<InfoMaterial> buscar(Criterio criterio) {
        List<CompletableFuture<InfoMaterial>> pendientes = new ArrayList<>();
        for (InfoMaterial info : fuente.buscar(criterio)) {
            pendientes.add(info.encontrado()
                    ? CompletableFuture.supplyAsync(() -> traducir(info), hilos)
                    : CompletableFuture.completedFuture(info));
        }
        List<InfoMaterial> traducidos = new ArrayList<>();
        for (CompletableFuture<InfoMaterial> f : pendientes) {
            traducidos.add(f.join());
        }
        return traducidos;
    }

    private InfoMaterial traducir(InfoMaterial info) {
        String descripcion = resumir(info.descripcion());
        return new InfoMaterial(
                conRespaldo(traductor.traducir(info.titulo()), info.titulo()),
                info.marca(),
                conRespaldo(traductor.traducir(descripcion), descripcion),
                null,
                info.precio(),
                info.urlImagen(),
                info.urlEnlace());
    }

    /** Primera frase de la descripción, o un recorte de como mucho MAX_DESCRIPCION caracteres. */
    static String resumir(String texto) {
        if (texto == null) {
            return "";
        }
        String t = texto.trim();
        int punto = t.indexOf(". ");
        if (punto > 30 && punto < MAX_DESCRIPCION) {
            return t.substring(0, punto + 1);
        }
        if (t.length() <= MAX_DESCRIPCION) {
            return t;
        }
        int corte = t.lastIndexOf(' ', MAX_DESCRIPCION);
        return t.substring(0, corte > 0 ? corte : MAX_DESCRIPCION) + "…";
    }

    private static String conRespaldo(String traducido, String original) {
        return traducido == null ? original : traducido;
    }
}
