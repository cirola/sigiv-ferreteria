package com.sigiv.modelo;

import java.math.BigDecimal;

public class Cliente {
    private int id;
    private String nombre;
    private String documento;
    private String telefono;
    private String email;
    private String direccion;
    private boolean tieneCtaCte;
    private BigDecimal limiteCredito = BigDecimal.ZERO;
    private BigDecimal saldoCtaCte = BigDecimal.ZERO;
    private boolean activo = true;

    public Cliente() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getDocumento() { return documento; }
    public void setDocumento(String v) { this.documento = v; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String v) { this.telefono = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String v) { this.direccion = v; }
    public boolean isTieneCtaCte() { return tieneCtaCte; }
    public void setTieneCtaCte(boolean v) { this.tieneCtaCte = v; }
    public BigDecimal getLimiteCredito() { return limiteCredito; }
    public void setLimiteCredito(BigDecimal v) { this.limiteCredito = v; }
    public BigDecimal getSaldoCtaCte() { return saldoCtaCte; }
    public void setSaldoCtaCte(BigDecimal v) { this.saldoCtaCte = v; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean v) { this.activo = v; }

    public BigDecimal creditoDisponible() {
        return limiteCredito.subtract(saldoCtaCte);
    }

    @Override
    public String toString() {
        return nombre + (documento != null ? " (" + documento + ")" : "");
    }
}
