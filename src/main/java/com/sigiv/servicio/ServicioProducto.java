package com.sigiv.servicio;

import com.sigiv.dao.ProductoDAO;
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
            throw new IllegalArgumentException("El código es obligatorio");
        if (p.getDescripcion() == null || p.getDescripcion().isBlank())
            throw new IllegalArgumentException("La descripción es obligatoria");
        if (p.getRubroId() <= 0)
            throw new IllegalArgumentException("Debe seleccionar un rubro");
        if (p.getPrecioCosto() == null || p.getPrecioCosto().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("El precio de costo debe ser >= 0");
        if (p.getPrecioVenta() == null || p.getPrecioVenta().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("El precio de venta debe ser >= 0");
        if (p.getStockActual() < 0) throw new IllegalArgumentException("Stock actual no puede ser negativo");
        if (p.getStockMinimo() < 0) throw new IllegalArgumentException("Stock mínimo no puede ser negativo");

        if (esAlta) {
            try {
                if (dao.buscarPorCodigo(p.getCodigo()).isPresent())
                    throw new IllegalArgumentException("Ya existe un producto con código " + p.getCodigo());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
