package com.sigiv.dao;

import com.sigiv.modelo.Cliente;
import com.sigiv.util.ConexionBD;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClienteDAO {

    private static final String SELECT_BASE =
            "SELECT id, nombre, documento, telefono, email, direccion, " +
            "       tiene_cta_cte, limite_credito, saldo_cta_cte, activo " +
            "FROM clientes ";

    public List<Cliente> listar() throws SQLException {
        String sql = SELECT_BASE + "WHERE activo = TRUE ORDER BY nombre";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapear(rs);
        }
    }

    public Optional<Cliente> buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE id = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<Cliente> l = mapear(rs);
                return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
            }
        }
    }

    /**
     * Actualiza saldo de cta cte y registra el movimiento dentro de la misma conexión (transacción).
     */
    public void registrarMovimiento(Connection c, int clienteId, String tipo,
                                    BigDecimal monto, String descripcion, Integer ventaId)
            throws SQLException {

        String sqlMov = "INSERT INTO movimientos_cta_cte (cliente_id, tipo, monto, descripcion, venta_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sqlMov)) {
            ps.setInt(1, clienteId);
            ps.setString(2, tipo);
            ps.setBigDecimal(3, monto);
            ps.setString(4, descripcion);
            if (ventaId == null) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, ventaId);
            ps.executeUpdate();
        }

        String sqlSaldo;
        if ("DEBE".equals(tipo)) {
            sqlSaldo = "UPDATE clientes SET saldo_cta_cte = saldo_cta_cte + ? WHERE id = ?";
        } else {
            sqlSaldo = "UPDATE clientes SET saldo_cta_cte = saldo_cta_cte - ? WHERE id = ?";
        }
        try (PreparedStatement ps = c.prepareStatement(sqlSaldo)) {
            ps.setBigDecimal(1, monto);
            ps.setInt(2, clienteId);
            ps.executeUpdate();
        }
    }

    private List<Cliente> mapear(ResultSet rs) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        while (rs.next()) {
            Cliente c = new Cliente();
            c.setId(rs.getInt("id"));
            c.setNombre(rs.getString("nombre"));
            c.setDocumento(rs.getString("documento"));
            c.setTelefono(rs.getString("telefono"));
            c.setEmail(rs.getString("email"));
            c.setDireccion(rs.getString("direccion"));
            c.setTieneCtaCte(rs.getBoolean("tiene_cta_cte"));
            c.setLimiteCredito(rs.getBigDecimal("limite_credito"));
            c.setSaldoCtaCte(rs.getBigDecimal("saldo_cta_cte"));
            c.setActivo(rs.getBoolean("activo"));
            lista.add(c);
        }
        return lista;
    }
}
