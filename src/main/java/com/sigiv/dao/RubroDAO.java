package com.sigiv.dao;

import com.sigiv.modelo.Rubro;
import com.sigiv.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RubroDAO {

    public List<Rubro> listar() throws SQLException {
        String sql = "SELECT id, nombre, margen_default FROM rubros ORDER BY nombre";
        List<Rubro> lista = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Rubro(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getBigDecimal("margen_default")
                ));
            }
        }
        return lista;
    }
}
