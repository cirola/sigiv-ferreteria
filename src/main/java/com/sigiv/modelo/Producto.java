package com.sigiv.modelo;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * Producto del inventario.
 *
 * <p>Implementa {@link Comparable} para definir un <b>orden natural</b> (por
 * descripción) y expone varios {@link Comparator} como estrategias de
 * ordenamiento. Esto habilita el <b>polimorfismo</b> en los algoritmos de
 * ordenación genéricos de {@code com.sigiv.util.Algoritmos}: el mismo método
 * ordena por precio, stock o descripción según el comparador recibido.</p>
 */
public class Producto implements Comparable<Producto> {

    /** Estrategias de ordenamiento reutilizables (polimorfismo vía Comparator). */
    public static final Comparator<Producto> POR_PRECIO =
            Comparator.comparing(Producto::getPrecioVenta);
    public static final Comparator<Producto> POR_STOCK =
            Comparator.comparingInt(Producto::getStockActual);
    public static final Comparator<Producto> POR_DESCRIPCION =
            Comparator.comparing(p -> p.getDescripcion().toLowerCase());
    public static final Comparator<Producto> POR_CODIGO =
            Comparator.comparing(Producto::getCodigo);

    private int id;
    private String codigo;
    private String descripcion;
    private int rubroId;
    private String rubroNombre;
    private Integer proveedorId;
    private BigDecimal precioCosto;
    private BigDecimal precioVenta;
    private int stockActual;
    private int stockMinimo;
    private boolean activo = true;

    public Producto() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String v) { this.codigo = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public int getRubroId() { return rubroId; }
    public void setRubroId(int v) { this.rubroId = v; }
    public String getRubroNombre() { return rubroNombre; }
    public void setRubroNombre(String v) { this.rubroNombre = v; }
    public Integer getProveedorId() { return proveedorId; }
    public void setProveedorId(Integer v) { this.proveedorId = v; }
    public BigDecimal getPrecioCosto() { return precioCosto; }
    public void setPrecioCosto(BigDecimal v) { this.precioCosto = v; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal v) { this.precioVenta = v; }
    public int getStockActual() { return stockActual; }
    public void setStockActual(int v) { this.stockActual = v; }
    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int v) { this.stockMinimo = v; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean v) { this.activo = v; }

    public boolean necesitaReposicion() {
        return stockActual <= stockMinimo;
    }

    /** Orden natural del producto: alfabético por descripción. */
    @Override
    public int compareTo(Producto otro) {
        return this.descripcion.compareToIgnoreCase(otro.descripcion);
    }

    @Override
    public String toString() {
        return codigo + " - " + descripcion;
    }
}
