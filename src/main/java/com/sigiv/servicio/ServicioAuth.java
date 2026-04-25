package com.sigiv.servicio;

import com.sigiv.dao.UsuarioDAO;
import com.sigiv.modelo.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

public class ServicioAuth {

    private final UsuarioDAO dao = new UsuarioDAO();
    private Usuario usuarioActual;

    public Optional<Usuario> autenticar(String nombreUsuario, String password) throws SQLException {
        Optional<Usuario> o = dao.buscarPorNombre(nombreUsuario);
        if (o.isEmpty()) return Optional.empty();
        Usuario u = o.get();
        if (BCrypt.checkpw(password, u.getPasswordHash())) {
            this.usuarioActual = u;
            return Optional.of(u);
        }
        return Optional.empty();
    }

    public Usuario getUsuarioActual() { return usuarioActual; }

    public void cerrarSesion() { this.usuarioActual = null; }

    public static String hashear(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }
}
