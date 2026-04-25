package com.sigiv.dao;

import com.sigiv.modelo.DetalleVenta;
import com.sigiv.modelo.Venta;
import com.sigiv.util.ConexionBD;

import java.sql.*;

public class VentaDAO {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    /**
     * Registra una venta de forma atómica:
     *  1. Inserta la venta.
     *  2. Inserta cada detalle y descuenta stock (valida disponibilidad).
     *  3. Si es cta. cte., registra movimiento DEBE y actualiza saldo.
     *  4. Escribe en bitácora.
     * Si algo falla, hace rollback.
     */
    public int registrar(Venta v) throws SQLException {
        Connection c = ConexionBD.get();
        try {
            c.setAutoCommit(false);

            String sqlVenta = "INSERT INTO ventas (usuario_id, cliente_id, forma_pago, total) " +
                    "VALUES (?, ?, ?, ?)";
            int ventaId;
            try (PreparedStatement ps = c.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, v.getUsuarioId());
                if (v.getClienteId() == null) ps.setNull(2, Types.INTEGER);
                else ps.setInt(2, v.getClienteId());
                ps.setString(3, v.getFormaPago().name());
                ps.setBigDecimal(4, v.getTotal());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("No se obtuvo id de venta");
                    ventaId = rs.getInt(1);
                    v.setId(ventaId);
                }
            }

            String sqlDet = "INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, " +
                    "  precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sqlDet)) {
                for (DetalleVenta d : v.getItems()) {
                    productoDAO.descontarStock(c, d.getProductoId(), d.getCantidad());
                    ps.setInt(1, ventaId);
                    ps.setInt(2, d.getProductoId());
                    ps.setInt(3, d.getCantidad());
                    ps.setBigDecimal(4, d.getPrecioUnitario());
                    ps.setBigDecimal(5, d.getSubtotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            if (v.getFormaPago() == Venta.FormaPago.CTA_CTE && v.getClienteId() != null) {
                clienteDAO.registrarMovimiento(c, v.getClienteId(), "DEBE",
                        v.getTotal(), "Venta #" + ventaId, ventaId);
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO bitacora (usuario_id, accion, detalle) VALUES (?, ?, ?)")) {
                ps.setInt(1, v.getUsuarioId());
                ps.setString(2, "VENTA_REGISTRADA");
                ps.setString(3, "Venta #" + ventaId + " total=" + v.getTotal() +
                        " forma=" + v.getFormaPago());
                ps.executeUpdate();
            }

            c.commit();
            return ventaId;
        } catch (SQLException e) {
            try { c.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            try { c.setAutoCommit(true); c.close(); } catch (SQLException ignored) {}
        }
    }
}
