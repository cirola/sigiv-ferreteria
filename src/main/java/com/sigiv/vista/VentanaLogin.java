package com.sigiv.vista;

import com.sigiv.modelo.Usuario;
import com.sigiv.servicio.ServicioAuth;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Optional;

public class VentanaLogin extends JFrame {

    private final ServicioAuth auth;
    private final JTextField txtUsuario = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);

    public VentanaLogin(ServicioAuth auth) {
        this.auth = auth;
        setTitle("SIGIV - Ingreso al sistema");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 220);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel titulo = new JLabel("Ferretería San Martín - SIGIV", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(titulo, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1; form.add(txtUsuario, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; form.add(txtPassword, gbc);

        add(form, BorderLayout.CENTER);

        JButton btnEntrar = new JButton("Ingresar");
        JButton btnSalir = new JButton("Salir");
        JPanel botones = new JPanel();
        botones.add(btnEntrar);
        botones.add(btnSalir);
        add(botones, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnEntrar);
        btnEntrar.addActionListener(e -> intentarLogin());
        btnSalir.addActionListener(e -> System.exit(0));
    }

    private void intentarLogin() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (usuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete usuario y contraseña",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Optional<Usuario> o = auth.autenticar(usuario, password);
            if (o.isPresent()) {
                dispose();
                new VentanaPrincipal(auth).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Usuario o contraseña incorrectos",
                        "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error de conexión a la base de datos:\n" + ex.getMessage(),
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        }
    }
}
