package com.sigiv.excepcion;

/**
 * Excepción de negocio para datos inválidos (campos obligatorios, valores fuera
 * de rango, etc.).
 *
 * <p>Extiende {@link IllegalArgumentException} de forma deliberada: así la capa
 * de presentación Swing existente —que captura {@code IllegalArgumentException}—
 * sigue funcionando sin cambios, mientras que el menú de consola puede capturar
 * los subtipos concretos para dar mensajes más específicos. Es un ejemplo de
 * <b>herencia</b> aplicada a la jerarquía de excepciones.</p>
 */
public class ValidacionException extends IllegalArgumentException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
