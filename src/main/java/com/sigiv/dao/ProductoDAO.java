package com.sigiv.dao;

import com.sigiv.modelo.Producto;
import com.sigiv.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoDAO {

    private static final String SELECT_BASE =
            "SELECT p.id, p.codigo, p.descripcion, p.rubro_id, r.nombre AS rubro_nombre, " +
            "       p.proveedor_id, p.precio_costo, p.precio_venta, " +
            "       p.stock_actual, p.stock_minimo, p.activo " +
            "FROM productos p JOIN rubros r ON r.id = p.rubro_id ";

    public List<Producto> listar() throws SQLException {
        String sql = SELECT_BASE + "WHERE p.activo = TRUE ORDER BY p.descripcion";
        return ejecutarLista(sql);
    }

    public List<Producto> buscar(String texto) throws SQLException {
        String sql = SELECT_BASE +
                "WHERE p.activo = TRUE AND (p.codigo LIKE ? OR p.descripcion LIKE ?) " +
                "ORDER BY p.descripcion";
        String like = "%" + texto + "%";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                return mapear(rs);
            }
        }
    }

    public Optional<Producto> buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.id = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<Producto> l = mapear(rs);
                return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
            }
        }
    }

    public Optional<Producto> buscarPorCodigo(String codigo) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.codigo = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                List<Producto> l = mapear(rs);
                return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
            }
        }
    }

    public int insertar(Producto p) throws SQLException {
        String sql = "INSERT INTO productos (codigo, descripcion, rubro_id, proveedor_id, " +
                "  precio_costo, precio_venta, stock_actual, stock_minimo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getDescripcion());
            ps.setInt(3, p.getRubroId());
            if (p.getProveedorId() == null) ps.setNull(4, Types.INTEGER);
            else ps.setInt(4, p.getProveedorId());
            ps.setBigDecimal(5, p.getPrecioCosto());
            ps.setBigDecimal(6, p.getPrecioVenta());
            ps.setInt(7, p.getStockActual());
            ps.setInt(8, p.getStockMinimo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                    return p.getId();
                }
                throw new SQLException("No se obtuvo id generado");
            }
        }
    }

    public void actualizar(Producto p) throws SQLException {
        String sql = "UPDATE productos SET codigo=?, descripcion=?, rubro_id=?, proveedor_id=?, " +
                "  precio_costo=?, precio_venta=?, stock_actual=?, stock_minimo=? " +
                "WHERE id=?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getDescripcion());
            ps.setInt(3, p.getRubroId());
            if (p.getProveedorId() == null) ps.setNull(4, Types.INTEGER);
            else ps.setInt(4, p.getProveedorId());
            ps.setBigDecimal(5, p.getPrecioCosto());
            ps.setBigDecimal(6, p.getPrecioVenta());
            ps.setInt(7, p.getStockActual());
            ps.setInt(8, p.getStockMinimo());
            ps.setInt(9, p.getId());
            ps.executeUpdate();
        }
    }

    public void darDeBaja(int id) throws SQLException {
        String sql = "UPDATE productos SET activo = FALSE WHERE id = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Descuenta stock desde una conexión ya abierta (usado por VentaDAO dentro de una transacción).
     * Valida que el stock sea suficiente en la misma sentencia.
     */
    public void descontarStock(Connection c, int productoId, int cantidad) throws SQLException {
        String sql = "UPDATE productos SET stock_actual = stock_actual - ? " +
                "WHERE id = ? AND stock_actual >= ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, productoId);
            ps.setInt(3, cantidad);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("Stock insuficiente para producto id=" + productoId);
            }
        }
    }

    private List<Producto> ejecutarLista(String sql) throws SQLException {
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapear(rs);
        }
    }

    private List<Producto> mapear(ResultSet rs) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        while (rs.next()) {
            Producto p = new Producto();
            p.setId(rs.getInt("id"));
            p.setCodigo(rs.getString("codigo"));
            p.setDescripcion(rs.getString("descripcion"));
            p.setRubroId(rs.getInt("rubro_id"));
            p.setRubroNombre(rs.getString("rubro_nombre"));
            int provId = rs.getInt("proveedor_id");
            p.setProveedorId(rs.wasNull() ? null : provId);
            p.setPrecioCosto(rs.getBigDecimal("precio_costo"));
            p.setPrecioVenta(rs.getBigDecimal("precio_venta"));
            p.setStockActual(rs.getInt("stock_actual"));
            p.setStockMinimo(rs.getInt("stock_minimo"));
            p.setActivo(rs.getBoolean("activo"));
            lista.add(p);
        }
        return lista;
    }
}
