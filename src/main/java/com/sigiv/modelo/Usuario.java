package com.sigiv.modelo;

public class Usuario {
    private int id;
    private String nombreUsuario;
    private String nombreCompleto;
    private String passwordHash;
    private Rol rol;
    private boolean activo;

    public Usuario() {}

    public Usuario(int id, String nombreUsuario, String nombreCompleto,
                   String passwordHash, Rol rol, boolean activo) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.nombreCompleto = nombreCompleto;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String v) { this.nombreUsuario = v; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String v) { this.nombreCompleto = v; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String v) { this.passwordHash = v; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombreCompleto + " (" + nombreUsuario + " - " + rol + ")";
    }
}
