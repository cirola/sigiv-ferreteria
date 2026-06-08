package com.sigiv;

import com.sigiv.consola.MenuConsola;
import com.sigiv.servicio.ServicioAuth;
import com.sigiv.vista.VentanaLogin;

import javax.swing.*;

/**
 * Punto de entrada del prototipo SIGIV-SM.
 *
 * <p>Admite dos modos de ejecución:</p>
 * <ul>
 *   <li>Sin argumentos: interfaz gráfica Swing (entregas AP1/AP2).</li>
 *   <li>Con {@code --consola}: menú de texto que demuestra los pilares de POO
 *       desarrollados en la AP3.</li>
 * </ul>
 */
public class App {
    public static void main(String[] args) {
        boolean modoConsola = args.length > 0 && args[0].equalsIgnoreCase("--consola");

        if (modoConsola) {
            new MenuConsola().iniciar();
            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        ServicioAuth auth = new ServicioAuth();
        SwingUtilities.invokeLater(() -> new VentanaLogin(auth).setVisible(true));
    }
}
