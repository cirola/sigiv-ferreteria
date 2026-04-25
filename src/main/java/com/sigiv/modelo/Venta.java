package com.sigiv.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {
    public enum FormaPago { EFECTIVO, TRANSFERENCIA, CTA_CTE }
    public enum Estado { CONFIRMADA, ANULADA }

    private int id;
    private LocalDateTime fecha = LocalDateTime.now();
    private int usuarioId;
    private Integer clienteId;
    private String clienteNombre;
    private FormaPago formaPago = FormaPago.EFECTIVO;
    private Estado estado = Estado.CONFIRMADA;
    private List<DetalleVenta> items = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime v) { this.fecha = v; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int v) { this.usuarioId = v; }
    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer v) { this.clienteId = v; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String v) { this.clienteNombre = v; }
    public FormaPago getFormaPago() { return formaPago; }
    public void setFormaPago(FormaPago v) { this.formaPago = v; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado v) { this.estado = v; }
    public List<DetalleVenta> getItems() { return items; }
    public void setItems(List<DetalleVenta> v) { this.items = v; }

    public BigDecimal getTotal() {
        return items.stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
