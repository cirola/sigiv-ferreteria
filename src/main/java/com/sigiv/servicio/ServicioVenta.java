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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

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

    /** Devuelve todas las ventas confirmadas (lista dinámica desde la BD). */
    public List<Venta> listar() throws SQLException {
        return ventaDAO.listar();
    }

    /**
     * Reporte de facturación agrupada por forma de pago.
     *
     * <p>Demuestra el uso <b>complementario</b> de las dos estructuras de la
     * consigna: la fuente de datos es un {@link java.util.ArrayList} de ventas
     * (colección dinámica, tamaño desconocido a priori) y la acumulación se hace
     * sobre un <b>arreglo</b> de tamaño fijo, indexado por el {@code ordinal()}
     * de cada forma de pago. El arreglo es la estructura natural aquí porque la
     * cantidad de formas de pago es conocida y constante
     * ({@code FormaPago.values().length}).</p>
     *
     * @param ventas lista de ventas a procesar
     * @return arreglo de totales paralelo a {@code Venta.FormaPago.values()}
     */
    public BigDecimal[] totalesPorFormaPago(List<Venta> ventas) {
        Venta.FormaPago[] formas = Venta.FormaPago.values();   // arreglo de tamaño fijo
        BigDecimal[] totales = new BigDecimal[formas.length];  // arreglo paralelo de acumuladores
        Arrays.fill(totales, BigDecimal.ZERO);

        for (Venta v : ventas) {                               // recorre el ArrayList
            int i = v.getFormaPago().ordinal();                // índice en el arreglo
            totales[i] = totales[i].add(v.getTotal());
        }
        return totales;
    }
}
