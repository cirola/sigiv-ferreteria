package com.sigiv.modelo;

/**
 * Clase ABSTRACTA que modela una persona/entidad de contacto del sistema.
 *
 * <p>Aplica dos pilares de la POO:</p>
 * <ul>
 *   <li><b>Abstracción</b>: define el contrato común (datos de contacto y
 *       operaciones) pero no puede instanciarse por sí misma; obliga a las
 *       subclases a completar el comportamiento específico mediante métodos
 *       abstractos.</li>
 *   <li><b>Encapsulamiento</b>: los atributos son {@code protected} y el acceso
 *       se realiza a través de métodos públicos (getters/setters), nunca por
 *       acceso directo desde otras capas.</li>
 * </ul>
 *
 * <p>Es la raíz de la jerarquía de <b>herencia</b>: {@link Cliente} y
 * {@link Proveedor} la extienden.</p>
 */
public abstract class Persona {

    protected int id;
    protected String nombre;
    protected String documento;
    protected String telefono;
    protected String email;
    protected String direccion;
    protected boolean activo = true;

    /** Constructor por defecto, requerido por los DAO al mapear desde la BD. */
    protected Persona() {
    }

    /** Constructor con los datos comunes a toda persona. */
    protected Persona(int id, String nombre, String documento,
                      String telefono, String email, String direccion) {
        this.id = id;
        this.nombre = nombre;
        this.documento = documento;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
    }

    // --- Encapsulamiento: acceso controlado al estado ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    /**
     * Método ABSTRACTO: cada subclase declara a qué tipo de entidad corresponde.
     * Es la base del POLIMORFISMO: la misma llamada produce resultados distintos
     * según el objeto concreto (Cliente / Proveedor).
     */
    public abstract String tipoEntidad();

    /**
     * Método ABSTRACTO: ficha resumida específica de cada subclase
     * (un cliente muestra su saldo; un proveedor muestra su CUIT, etc.).
     */
    public abstract String fichaResumen();

    /** Método CONCRETO heredado por todas las subclases. */
    public String datosContacto() {
        return String.format("Tel: %s | Email: %s",
                telefono != null && !telefono.isBlank() ? telefono : "-",
                email != null && !email.isBlank() ? email : "-");
    }

    @Override
    public String toString() {
        return nombre + (documento != null ? " (" + documento + ")" : "");
    }
}
