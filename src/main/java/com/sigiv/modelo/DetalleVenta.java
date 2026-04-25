package com.sigiv.modelo;

import java.math.BigDecimal;

public class DetalleVenta {
    private int productoId;
    private String productoCodigo;
    private String productoDescripcion;
    private int cantidad;
    private BigDecimal precioUnitario;

    public DetalleVenta() {}

    public DetalleVenta(Producto p, int cantidad) {
        this.productoId = p.getId();
        this.productoCodigo = p.getCodigo();
        this.productoDescripcion = p.getDescripcion();
        this.cantidad = cantidad;
        this.precioUnitario = p.getPrecioVenta();
    }

    public int getProductoId() { return productoId; }
    public void setProductoId(int v) { this.productoId = v; }
    public String getProductoCodigo() { return productoCodigo; }
    public void setProductoCodigo(String v) { this.productoCodigo = v; }
    public String getProductoDescripcion() { return productoDescripcion; }
    public void setProductoDescripcion(String v) { this.productoDescripcion = v; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int v) { this.cantidad = v; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal v) { this.precioUnitario = v; }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
