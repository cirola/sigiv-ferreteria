package com.sigiv.excepcion;

/**
 * Excepción <b>chequeada</b> (extiende {@link Exception}): el compilador obliga
 * a declararla con {@code throws} y a capturarla con try/catch. Se usa cuando
 * una búsqueda no encuentra la entidad pedida (por código, por id, etc.).
 *
 * <p>Complementa a las excepciones no chequeadas del paquete y permite mostrar
 * en el informe el manejo de los dos tipos de excepciones de Java.</p>
 */
public class EntidadNoEncontradaException extends Exception {

    public EntidadNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
