package com.sigiv.modelo;

import java.math.BigDecimal;

/**
 * Cliente de la ferretería. <b>Hereda</b> de {@link Persona} los datos de
 * contacto y agrega los atributos propios de la cuenta corriente.
 *
 * <p>Aplica <b>herencia</b> (extends Persona) y <b>polimorfismo</b>
 * (sobrescribe los métodos abstractos {@code tipoEntidad} y
 * {@code fichaResumen}).</p>
 */
public class Cliente extends Persona {

    private boolean tieneCtaCte;
    private BigDecimal limiteCredito = BigDecimal.ZERO;
    private BigDecimal saldoCtaCte = BigDecimal.ZERO;

    public Cliente() {
        super();
    }

    public Cliente(int id, String nombre, String documento, String telefono,
                   String email, String direccion, boolean tieneCtaCte,
                   BigDecimal limiteCredito, BigDecimal saldoCtaCte) {
        super(id, nombre, documento, telefono, email, direccion);
        this.tieneCtaCte = tieneCtaCte;
        this.limiteCredito = limiteCredito;
        this.saldoCtaCte = saldoCtaCte;
    }

    public boolean isTieneCtaCte() { return tieneCtaCte; }
    public void setTieneCtaCte(boolean v) { this.tieneCtaCte = v; }
    public BigDecimal getLimiteCredito() { return limiteCredito; }
    public void setLimiteCredito(BigDecimal v) { this.limiteCredito = v; }
    public BigDecimal getSaldoCtaCte() { return saldoCtaCte; }
    public void setSaldoCtaCte(BigDecimal v) { this.saldoCtaCte = v; }

    /** Regla de negocio propia del cliente: crédito que aún puede usar. */
    public BigDecimal creditoDisponible() {
        return limiteCredito.subtract(saldoCtaCte);
    }

    // --- Polimorfismo: implementación concreta de los métodos abstractos ---

    @Override
    public String tipoEntidad() {
        return "CLIENTE";
    }

    @Override
    public String fichaResumen() {
        if (tieneCtaCte) {
            return String.format("%s | Cta.Cte: saldo $%s / límite $%s (disp. $%s)",
                    nombre, saldoCtaCte, limiteCredito, creditoDisponible());
        }
        return nombre + " | Contado";
    }
}
