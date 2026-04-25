package com.sigiv.servicio;

import com.sigiv.dao.ClienteDAO;
import com.sigiv.dao.VentaDAO;
import com.sigiv.modelo.Cliente;
import com.sigiv.modelo.Venta;

import java.sql.SQLException;

public class ServicioVenta {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    public int registrar(Venta v) throws SQLException {
        if (v.getItems() == null || v.getItems().isEmpty())
            throw new IllegalArgumentException("La venta debe tener al menos un ítem");
        if (v.getUsuarioId() <= 0)
            throw new IllegalArgumentException("Usuario no válido");

        if (v.getFormaPago() == Venta.FormaPago.CTA_CTE) {
            if (v.getClienteId() == null)
                throw new IllegalArgumentException("Debe seleccionar un cliente para venta a cuenta corriente");
            Cliente c = clienteDAO.buscarPorId(v.getClienteId())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente inexistente"));
            if (!c.isTieneCtaCte())
                throw new IllegalArgumentException("El cliente no tiene cuenta corriente habilitada");
            if (v.getTotal().compareTo(c.creditoDisponible()) > 0)
                throw new IllegalArgumentException("Supera el crédito disponible: $" + c.creditoDisponible());
        }

        return ventaDAO.registrar(v);
    }
}
