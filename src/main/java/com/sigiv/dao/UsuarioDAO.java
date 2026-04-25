package com.sigiv.dao;

import com.sigiv.modelo.Rol;
import com.sigiv.modelo.Usuario;
import com.sigiv.util.ConexionBD;

import java.sql.*;
import java.util.Optional;

public class UsuarioDAO {

    private static final String SQL_POR_NOMBRE =
            "SELECT u.id, u.nombre_usuario, u.nombre_completo, u.password_hash, " +
            "       r.nombre AS rol_nombre, u.activo " +
            "FROM usuarios u JOIN roles r ON r.id = u.rol_id " +
            "WHERE u.nombre_usuario = ? AND u.activo = TRUE";

    public Optional<Usuario> buscarPorNombre(String nombreUsuario) throws SQLException {
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(SQL_POR_NOMBRE)) {

            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario(
                            rs.getInt("id"),
                            rs.getString("nombre_usuario"),
                            rs.getString("nombre_completo"),
                            rs.getString("password_hash"),
                            Rol.fromString(rs.getString("rol_nombre")),
                            rs.getBoolean("activo")
                    );
                    return Optional.of(u);
                }
                return Optional.empty();
            }
        }
    }
}
