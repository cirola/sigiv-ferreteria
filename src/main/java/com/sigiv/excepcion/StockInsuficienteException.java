package com.sigiv.excepcion;

/**
 * Se lanza cuando se intenta vender más unidades de las disponibles en stock.
 * Hereda de {@link ValidacionException} (y por lo tanto es no chequeada),
 * manteniendo compatibilidad con la capa Swing.
 */
public class StockInsuficienteException extends ValidacionException {

    private final String codigoProducto;
    private final int solicitado;
    private final int disponible;

    public StockInsuficienteException(String codigoProducto, int solicitado, int disponible) {
        super(String.format(
                "Stock insuficiente de '%s': solicitado %d, disponible %d",
                codigoProducto, solicitado, disponible));
        this.codigoProducto = codigoProducto;
        this.solicitado = solicitado;
        this.disponible = disponible;
    }

    public String getCodigoProducto() { return codigoProducto; }
    public int getSolicitado() { return solicitado; }
    public int getDisponible() { return disponible; }
}
