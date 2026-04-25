package com.sigiv.vista;

import com.sigiv.dao.ClienteDAO;
import com.sigiv.modelo.*;
import com.sigiv.servicio.ServicioProducto;
import com.sigiv.servicio.ServicioVenta;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Optional;

public class PanelVenta extends JPanel {

    private final Usuario usuarioActual;
    private final ServicioProducto servicioProd = new ServicioProducto();
    private final ServicioVenta servicioVenta = new ServicioVenta();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    private final Venta venta = new Venta();

    private final JTextField txtCodigo = new JTextField(12);
    private final JTextField txtCantidad = new JTextField("1", 5);
    private final JComboBox<Cliente> cbCliente = new JComboBox<>();
    private final JComboBox<Venta.FormaPago> cbFormaPago = new JComboBox<>(Venta.FormaPago.values());

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Código", "Descripción", "Cant.", "P. Unit.", "Subtotal"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblTotal = new JLabel("Total: $ 0.00");

    public PanelVenta(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        venta.setUsuarioId(usuarioActual.getId());

        setLayout(new BorderLayout(6, 6));
        add(buildPanelEntrada(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(buildPanelFooter(), BorderLayout.SOUTH);

        cargarClientes();
    }

    private JComponent buildPanelEntrada() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel("Código:"));
        p.add(txtCodigo);
        p.add(new JLabel("Cant.:"));
        p.add(txtCantidad);
        JButton btnAgregar = new JButton("Agregar");
        JButton btnQuitar = new JButton("Quitar ítem");
        p.add(btnAgregar);
        p.add(btnQuitar);

        btnAgregar.addActionListener(e -> agregarItem());
        btnQuitar.addActionListener(e -> quitarItem());
        txtCodigo.addActionListener(e -> agregarItem());
        return p;
    }

    private JComponent buildPanelFooter() {
        JPanel bottom = new JPanel(new BorderLayout());

        JPanel datos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datos.add(new JLabel("Cliente:"));
        cbCliente.setPreferredSize(new Dimension(260, 25));
        datos.add(cbCliente);
        datos.add(new JLabel("Forma de pago:"));
        datos.add(cbFormaPago);
        bottom.add(datos, BorderLayout.WEST);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 16f));
        acciones.add(lblTotal);
        JButton btnConfirmar = new JButton("Confirmar venta");
        JButton btnCancelar = new JButton("Cancelar");
        acciones.add(btnConfirmar);
        acciones.add(btnCancelar);
        bottom.add(acciones, BorderLayout.EAST);

        btnConfirmar.addActionListener(e -> confirmar());
        btnCancelar.addActionListener(e -> nuevaVenta());

        return bottom;
    }

    private void cargarClientes() {
        cbCliente.removeAllItems();
        try {
            for (Cliente c : clienteDAO.listar()) cbCliente.addItem(c);
        } catch (SQLException e) { error(e); }
    }

    private void agregarItem() {
        String codigo = txtCodigo.getText().trim();
        if (codigo.isEmpty()) return;
        int cant;
        try { cant = Integer.parseInt(txtCantidad.getText().trim()); }
        catch (NumberFormatException e) { error("Cantidad inválida"); return; }
        if (cant <= 0) { error("La cantidad debe ser mayor a 0"); return; }

        try {
            Optional<Producto> op = servicioProd.buscarPorCodigo(codigo);
            if (op.isEmpty()) { error("Producto no encontrado: " + codigo); return; }
            Producto p = op.get();
            if (p.getStockActual() < cant) {
                error("Stock insuficiente. Stock actual: " + p.getStockActual());
                return;
            }
            venta.getItems().add(new DetalleVenta(p, cant));
            refrescarTabla();
            txtCodigo.setText(""); txtCantidad.setText("1"); txtCodigo.requestFocus();
        } catch (SQLException e) { error(e); }
    }

    private void quitarItem() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        venta.getItems().remove(fila);
        refrescarTabla();
    }

    private void refrescarTabla() {
        modelo.setRowCount(0);
        for (DetalleVenta d : venta.getItems()) {
            modelo.addRow(new Object[]{
                    d.getProductoCodigo(), d.getProductoDescripcion(),
                    d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()
            });
        }
        lblTotal.setText("Total: $ " + venta.getTotal().toPlainString());
    }

    private void confirmar() {
        if (venta.getItems().isEmpty()) { error("Agregue al menos un producto"); return; }
        Cliente c = (Cliente) cbCliente.getSelectedItem();
        if (c != null && c.getId() > 1) venta.setClienteId(c.getId()); else venta.setClienteId(null);
        venta.setFormaPago((Venta.FormaPago) cbFormaPago.getSelectedItem());

        try {
            int id = servicioVenta.registrar(venta);
            JOptionPane.showMessageDialog(this,
                    "Venta #" + id + " registrada.\nTotal: $ " + venta.getTotal(),
                    "Venta confirmada", JOptionPane.INFORMATION_MESSAGE);
            nuevaVenta();
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void nuevaVenta() {
        venta.setId(0);
        venta.setClienteId(null);
        venta.getItems().clear();
        venta.setFormaPago(Venta.FormaPago.EFECTIVO);
        cbFormaPago.setSelectedItem(Venta.FormaPago.EFECTIVO);
        refrescarTabla();
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atención", JOptionPane.WARNING_MESSAGE);
    }
    private void error(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
