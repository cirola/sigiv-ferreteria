package com.sigiv.vista;

import com.sigiv.modelo.Rol;
import com.sigiv.modelo.Usuario;
import com.sigiv.servicio.ServicioAuth;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private final ServicioAuth auth;

    public VentanaPrincipal(ServicioAuth auth) {
        this.auth = auth;
        Usuario u = auth.getUsuarioActual();

        setTitle("SIGIV - Ferretería San Martín  |  " + u.getNombreCompleto() + " (" + u.getRol() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Productos", new PanelProductos());
        tabs.addTab("Nueva venta", new PanelVenta(u));

        add(tabs, BorderLayout.CENTER);
        add(buildBarraEstado(u), BorderLayout.SOUTH);
        setJMenuBar(buildMenu());

        if (u.getRol() != Rol.ADMIN) {
            tabs.setEnabledAt(0, true); // Productos: consulta habilitada; el ABM se valida dentro
        }
    }

    private JMenuBar buildMenu() {
        JMenuBar mb = new JMenuBar();
        JMenu menuSistema = new JMenu("Sistema");
        JMenuItem miCerrar = new JMenuItem("Cerrar sesión");
        JMenuItem miSalir = new JMenuItem("Salir");
        miCerrar.addActionListener(e -> {
            auth.cerrarSesion();
            dispose();
            new VentanaLogin(auth).setVisible(true);
        });
        miSalir.addActionListener(e -> System.exit(0));
        menuSistema.add(miCerrar);
        menuSistema.addSeparator();
        menuSistema.add(miSalir);
        mb.add(menuSistema);
        return mb;
    }

    private JComponent buildBarraEstado(Usuario u) {
        JLabel lbl = new JLabel("  Usuario: " + u.getNombreUsuario() + "  |  Rol: " + u.getRol());
        lbl.setBorder(BorderFactory.createEtchedBorder());
        return lbl;
    }
}
