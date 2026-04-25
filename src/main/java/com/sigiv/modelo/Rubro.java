package com.sigiv.modelo;

import java.math.BigDecimal;

public class Rubro {
    private int id;
    private String nombre;
    private BigDecimal margenDefault;

    public Rubro() {}

    public Rubro(int id, String nombre, BigDecimal margenDefault) {
        this.id = id;
        this.nombre = nombre;
        this.margenDefault = margenDefault;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
    public BigDecimal getMargenDefault() { return margenDefault; }
    public void setMargenDefault(BigDecimal m) { this.margenDefault = m; }

    @Override public String toString() { return nombre; }
}
