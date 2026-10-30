package deportes.datos;

import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Decorador de un traductor: guarda cada traducción en la base de datos
 * (tabla {@code traduccion}) y no vuelve a pedirla nunca. Así cada texto se
 * traduce una sola vez, el límite diario del servicio dura mucho más y, al
 * subir la base de datos a Git, el resto del equipo ve los textos en español
 * sin gastar traducciones.
 */
public class TraductorConMemoria implements Traductor {

    static final String TABLA = """
            CREATE TABLE IF NOT EXISTS traduccion (
                original  TEXT PRIMARY KEY,
                traducido TEXT NOT NULL,
                fecha     TEXT NOT NULL
            )""";

    private final Traductor traductor;
    private final Path archivo;
    private boolean tablaCreada;

    public TraductorConMemoria(Traductor traductor, Path archivoBaseDatos) {
        this.traductor = Objects.requireNonNull(traductor, "traductor");
        this.archivo = archivoBaseDatos;
    }

    @Override
    public String traducir(String textoIngles) {
        if (textoIngles == null || textoIngles.isBlank()) {
            return null;
        }
        String guardada = buscarGuardada(textoIngles);
        if (guardada != null) {
            return guardada;
        }
        String nueva = traductor.traducir(textoIngles);
        if (nueva != null) {
            guardar(textoIngles, nueva);
        }
        return nueva;
    }

    private synchronized String buscarGuardada(String original) {
        try (Connection c = conectar();
             PreparedStatement ps = c.prepareStatement("SELECT traducido FROM traduccion WHERE original = ?")) {
            ps.setString(1, original);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    private synchronized void guardar(String original, String traducido) {
        try (Connection c = conectar();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT OR REPLACE INTO traduccion (original, traducido, fecha) VALUES (?, ?, ?)")) {
            ps.setString(1, original);
            ps.setString(2, traducido);
            ps.setString(3, LocalDate.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            // Si no se puede guardar, la traducción sirve igualmente para esta vez
        }
    }

    private Connection conectar() throws SQLException {
        Connection c = DriverManager.getConnection("jdbc:sqlite:" + archivo);
        if (!tablaCreada) {
            try (Statement st = c.createStatement()) {
                st.execute(TABLA);
            }
            tablaCreada = true;
        }
        return c;
    }
}
