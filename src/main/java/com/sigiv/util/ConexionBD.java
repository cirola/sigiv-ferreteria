package com.sigiv.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionBD {

    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream in = ConexionBD.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró config.properties en el classpath");
            }
            Properties p = new Properties();
            p.load(in);
            url = p.getProperty("db.url");
            user = p.getProperty("db.user");
            password = p.getProperty("db.password");
        } catch (IOException e) {
            throw new IllegalStateException("Error leyendo config.properties", e);
        }
    }

    private ConexionBD() {}

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
