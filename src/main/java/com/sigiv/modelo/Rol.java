package com.sigiv.modelo;

public enum Rol {
    ADMIN, VENDEDOR;

    public static Rol fromString(String s) {
        return Rol.valueOf(s.toUpperCase());
    }
}
