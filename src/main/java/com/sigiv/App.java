package com.sigiv;

import com.sigiv.servicio.ServicioAuth;
import com.sigiv.vista.VentanaLogin;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        ServicioAuth auth = new ServicioAuth();
        SwingUtilities.invokeLater(() -> new VentanaLogin(auth).setVisible(true));
    }
}
