package com.sigiv.modelo;

import java.math.BigDecimal;

public class Producto {
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

    @Override
    public String toString() {
        return codigo + " - " + descripcion;
    }
}
