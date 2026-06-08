package com.sigiv.servicio;

import com.sigiv.dao.ClienteDAO;
import com.sigiv.dao.ProductoDAO;
import com.sigiv.dao.VentaDAO;
import com.sigiv.excepcion.CreditoExcedidoException;
import com.sigiv.excepcion.StockInsuficienteException;
import com.sigiv.excepcion.ValidacionException;
import com.sigiv.modelo.Cliente;
import com.sigiv.modelo.DetalleVenta;
import com.sigiv.modelo.Producto;
import com.sigiv.modelo.Venta;

import java.sql.SQLException;

public class ServicioVenta {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    public int registrar(Venta v) throws SQLException {
        if (v.getItems() == null || v.getItems().isEmpty())
            throw new ValidacionException("La venta debe tener al menos un ítem");
        if (v.getUsuarioId() <= 0)
            throw new ValidacionException("Usuario no válido");

        // Validación de stock previa a la transacción (defensa en profundidad:
        // la BD vuelve a verificarlo dentro de la transacción atómica).
        for (DetalleVenta d : v.getItems()) {
            if (d.getCantidad() <= 0)
                throw new ValidacionException("La cantidad debe ser positiva");
            Producto p = productoDAO.buscarPorId(d.getProductoId())
                    .orElseThrow(() -> new ValidacionException(
                            "Producto inexistente id=" + d.getProductoId()));
            if (d.getCantidad() > p.getStockActual())
                throw new StockInsuficienteException(
                        p.getCodigo(), d.getCantidad(), p.getStockActual());
        }

        if (v.getFormaPago() == Venta.FormaPago.CTA_CTE) {
            if (v.getClienteId() == null)
                throw new ValidacionException(
                        "Debe seleccionar un cliente para venta a cuenta corriente");
            Cliente c = clienteDAO.buscarPorId(v.getClienteId())
                    .orElseThrow(() -> new ValidacionException("Cliente inexistente"));
            if (!c.isTieneCtaCte())
                throw new ValidacionException(
                        "El cliente no tiene cuenta corriente habilitada");
            if (v.getTotal().compareTo(c.creditoDisponible()) > 0)
                throw new CreditoExcedidoException(v.getTotal(), c.creditoDisponible());
        }

        return ventaDAO.registrar(v);
    }
}
