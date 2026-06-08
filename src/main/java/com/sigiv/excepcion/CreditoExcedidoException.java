package com.sigiv.excepcion;

import java.math.BigDecimal;

/**
 * Se lanza cuando una venta a cuenta corriente supera el crédito disponible del
 * cliente. Hereda de {@link ValidacionException}.
 */
public class CreditoExcedidoException extends ValidacionException {

    public CreditoExcedidoException(BigDecimal total, BigDecimal disponible) {
        super(String.format(
                "La venta ($%s) supera el crédito disponible ($%s) del cliente",
                total, disponible));
    }
}
