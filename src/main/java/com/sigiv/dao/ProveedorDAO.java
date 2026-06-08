package com.sigiv.dao;

import com.sigiv.modelo.Proveedor;
import com.sigiv.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code proveedores}. Mapea las columnas
 * {@code razon_social} y {@code cuit} a los atributos heredados nombre/documento
 * de {@link Proveedor}.
 */
public class ProveedorDAO {

    public List<Proveedor> listar() throws SQLException {
        String sql = "SELECT id, razon_social, cuit, telefono, email, direccion " +
                "FROM proveedores WHERE activo = TRUE ORDER BY razon_social";
        List<Proveedor> lista = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Proveedor(
                        rs.getInt("id"),
                        rs.getString("razon_social"),
                        rs.getString("cuit"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("direccion")
                ));
            }
        }
        return lista;
    }
}
