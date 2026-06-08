package com.sigiv.modelo;

/**
 * Proveedor de mercadería. Segunda subclase de {@link Persona}, demuestra que
 * la <b>herencia</b> permite reutilizar el contrato común y que el
 * <b>polimorfismo</b> da una implementación distinta de los métodos abstractos
 * respecto de {@link Cliente}.
 *
 * <p>En la base de datos el "nombre" se corresponde con la razón social y el
 * "documento" con el CUIT.</p>
 */
public class Proveedor extends Persona {

    public Proveedor() {
        super();
    }

    public Proveedor(int id, String razonSocial, String cuit,
                     String telefono, String email, String direccion) {
        super(id, razonSocial, cuit, telefono, email, direccion);
    }

    /** Alias semántico sobre el atributo heredado {@code nombre}. */
    public String getRazonSocial() { return nombre; }
    public void setRazonSocial(String razonSocial) { this.nombre = razonSocial; }

    /** Alias semántico sobre el atributo heredado {@code documento}. */
    public String getCuit() { return documento; }
    public void setCuit(String cuit) { this.documento = cuit; }

    // --- Polimorfismo: misma firma, comportamiento propio del proveedor ---

    @Override
    public String tipoEntidad() {
        return "PROVEEDOR";
    }

    @Override
    public String fichaResumen() {
        return String.format("%s | CUIT %s | %s",
                nombre,
                documento != null ? documento : "-",
                datosContacto());
    }
}
