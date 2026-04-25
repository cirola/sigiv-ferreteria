package com.sigiv.vista;

import com.sigiv.dao.RubroDAO;
import com.sigiv.modelo.Producto;
import com.sigiv.modelo.Rubro;
import com.sigiv.servicio.ServicioProducto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class PanelProductos extends JPanel {

    private final ServicioProducto servicio = new ServicioProducto();
    private final RubroDAO rubroDAO = new RubroDAO();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Código", "Descripción", "Rubro",
                    "P. Costo", "P. Venta", "Stock", "Stock Mín."}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JTextField txtBuscar = new JTextField(25);

    public PanelProductos() {
        setLayout(new BorderLayout(6, 6));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Buscar:"));
        top.add(txtBuscar);
        JButton btnBuscar = new JButton("Buscar");
        JButton btnRefrescar = new JButton("Refrescar");
        top.add(btnBuscar);
        top.add(btnRefrescar);
        add(top, BorderLayout.NORTH);

        tabla.getColumnModel().getColumn(0).setMaxWidth(50);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnNuevo = new JButton("Nuevo");
        JButton btnEditar = new JButton("Editar");
        JButton btnBaja = new JButton("Dar de baja");
        bot.add(btnNuevo);
        bot.add(btnEditar);
        bot.add(btnBaja);
        add(bot, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> cargar(txtBuscar.getText()));
        btnRefrescar.addActionListener(e -> { txtBuscar.setText(""); cargar(""); });
        btnNuevo.addActionListener(e -> abrirDialogo(null));
        btnEditar.addActionListener(e -> editarSeleccionado());
        btnBaja.addActionListener(e -> darDeBajaSeleccionado());

        cargar("");
    }

    private void cargar(String filtro) {
        try {
            List<Producto> lista = servicio.buscar(filtro);
            modelo.setRowCount(0);
            for (Producto p : lista) {
                modelo.addRow(new Object[]{
                        p.getId(), p.getCodigo(), p.getDescripcion(), p.getRubroNombre(),
                        p.getPrecioCosto(), p.getPrecioVenta(),
                        p.getStockActual(), p.getStockMinimo()
                });
            }
        } catch (SQLException e) {
            error(e);
        }
    }

    private Producto seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto");
            return null;
        }
        int id = (int) modelo.getValueAt(fila, 0);
        try {
            return servicio.buscar("").stream()
                    .filter(p -> p.getId() == id).findFirst().orElse(null);
        } catch (SQLException e) {
            error(e); return null;
        }
    }

    private void editarSeleccionado() {
        Producto p = seleccionado();
        if (p != null) abrirDialogo(p);
    }

    private void darDeBajaSeleccionado() {
        Producto p = seleccionado();
        if (p == null) return;
        int op = JOptionPane.showConfirmDialog(this,
                "¿Dar de baja " + p.getCodigo() + " - " + p.getDescripcion() + "?",
                "Confirmar baja", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            try {
                servicio.darDeBaja(p.getId());
                cargar(txtBuscar.getText());
            } catch (SQLException e) { error(e); }
        }
    }

    private void abrirDialogo(Producto existente) {
        JTextField txtCodigo = new JTextField(existente != null ? existente.getCodigo() : "");
        JTextField txtDesc = new JTextField(existente != null ? existente.getDescripcion() : "");
        JComboBox<Rubro> cbRubro = new JComboBox<>();
        JTextField txtCosto = new JTextField(existente != null ? existente.getPrecioCosto().toPlainString() : "0");
        JTextField txtVenta = new JTextField(existente != null ? existente.getPrecioVenta().toPlainString() : "0");
        JTextField txtStock = new JTextField(String.valueOf(existente != null ? existente.getStockActual() : 0));
        JTextField txtStockMin = new JTextField(String.valueOf(existente != null ? existente.getStockMinimo() : 0));

        try {
            for (Rubro r : rubroDAO.listar()) {
                cbRubro.addItem(r);
                if (existente != null && r.getId() == existente.getRubroId()) cbRubro.setSelectedItem(r);
            }
        } catch (SQLException e) { error(e); return; }

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Código:")); form.add(txtCodigo);
        form.add(new JLabel("Descripción:")); form.add(txtDesc);
        form.add(new JLabel("Rubro:")); form.add(cbRubro);
        form.add(new JLabel("Precio costo:")); form.add(txtCosto);
        form.add(new JLabel("Precio venta:")); form.add(txtVenta);
        form.add(new JLabel("Stock actual:")); form.add(txtStock);
        form.add(new JLabel("Stock mínimo:")); form.add(txtStockMin);

        int res = JOptionPane.showConfirmDialog(this, form,
                existente == null ? "Nuevo producto" : "Editar producto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            Producto p = existente != null ? existente : new Producto();
            p.setCodigo(txtCodigo.getText().trim());
            p.setDescripcion(txtDesc.getText().trim());
            Rubro r = (Rubro) cbRubro.getSelectedItem();
            if (r != null) p.setRubroId(r.getId());
            p.setPrecioCosto(new BigDecimal(txtCosto.getText().trim()));
            p.setPrecioVenta(new BigDecimal(txtVenta.getText().trim()));
            p.setStockActual(Integer.parseInt(txtStock.getText().trim()));
            p.setStockMinimo(Integer.parseInt(txtStockMin.getText().trim()));

            if (existente == null) servicio.crear(p);
            else servicio.actualizar(p);
            cargar(txtBuscar.getText());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            error(e);
        }
    }

    private void error(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
