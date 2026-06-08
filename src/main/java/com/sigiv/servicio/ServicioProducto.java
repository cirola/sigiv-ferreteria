package com.sigiv.servicio;

import com.sigiv.dao.ProductoDAO;
import com.sigiv.excepcion.EntidadNoEncontradaException;
import com.sigiv.excepcion.ValidacionException;
import com.sigiv.modelo.Producto;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ServicioProducto {

    private final ProductoDAO dao = new ProductoDAO();

    public List<Producto> listar() throws SQLException { return dao.listar(); }

    public List<Producto> buscar(String texto) throws SQLException {
        if (texto == null || texto.isBlank()) return dao.listar();
        return dao.buscar(texto.trim());
    }

    public Optional<Producto> buscarPorCodigo(String codigo) throws SQLException {
        return dao.buscarPorCodigo(codigo);
    }

    /**
     * Variante que usa una excepción <b>chequeada</b>: si el producto no existe,
     * obliga al llamador a manejar {@link EntidadNoEncontradaException}.
     */
    public Producto obtenerPorCodigo(String codigo)
            throws SQLException, EntidadNoEncontradaException {
        return dao.buscarPorCodigo(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No existe un producto con código '" + codigo + "'"));
    }

    public void crear(Producto p) throws SQLException {
        validar(p, true);
        dao.insertar(p);
    }

    public void actualizar(Producto p) throws SQLException {
        validar(p, false);
        dao.actualizar(p);
    }

    public void darDeBaja(int id) throws SQLException { dao.darDeBaja(id); }

    private void validar(Producto p, boolean esAlta) {
        if (p.getCodigo() == null || p.getCodigo().isBlank())
            throw new ValidacionException("El código es obligatorio");
        if (p.getDescripcion() == null || p.getDescripcion().isBlank())
            throw new ValidacionException("La descripción es obligatoria");
        if (p.getRubroId() <= 0)
            throw new ValidacionException("Debe seleccionar un rubro");
        if (p.getPrecioCosto() == null || p.getPrecioCosto().compareTo(BigDecimal.ZERO) < 0)
            throw new ValidacionException("El precio de costo debe ser >= 0");
        if (p.getPrecioVenta() == null || p.getPrecioVenta().compareTo(BigDecimal.ZERO) < 0)
            throw new ValidacionException("El precio de venta debe ser >= 0");
        if (p.getStockActual() < 0)
            throw new ValidacionException("Stock actual no puede ser negativo");
        if (p.getStockMinimo() < 0)
            throw new ValidacionException("Stock mínimo no puede ser negativo");

        if (esAlta) {
            try {
                if (dao.buscarPorCodigo(p.getCodigo()).isPresent())
                    throw new ValidacionException(
                            "Ya existe un producto con código " + p.getCodigo());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
