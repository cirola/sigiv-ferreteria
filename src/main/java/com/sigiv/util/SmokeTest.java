package com.sigiv.util;

import com.sigiv.dao.ClienteDAO;
import com.sigiv.dao.ProductoDAO;
import com.sigiv.modelo.*;
import com.sigiv.servicio.ServicioAuth;
import com.sigiv.servicio.ServicioProducto;
import com.sigiv.servicio.ServicioVenta;

import java.util.List;
import java.util.Optional;

/**
 * Smoke test no-GUI del prototipo SIGIV.
 * Verifica: conexión BD, login, listado de productos, creación de venta, descuento de stock.
 * Ejecutar: java -cp "target/classes:$(cat target/cp.txt)" com.sigiv.util.SmokeTest
 */
public class SmokeTest {

    public static void main(String[] args) throws Exception {
        System.out.println("=== SIGIV Smoke Test ===\n");

        // 1) Login
        ServicioAuth auth = new ServicioAuth();
        Optional<Usuario> ou = auth.autenticar("admin", "admin123");
        if (ou.isEmpty()) { fail("Login admin/admin123 falló"); }
        Usuario u = ou.get();
        ok("Login OK: " + u);

        // Login errado
        if (auth.autenticar("admin", "wrong").isPresent()) fail("Login con password mal no debería funcionar");
        ok("Login con password incorrecta → rechazado");

        // 2) Listar productos
        ServicioProducto sp = new ServicioProducto();
        List<Producto> productos = sp.listar();
        ok("Productos cargados: " + productos.size());
        productos.stream().limit(3).forEach(p ->
                System.out.println("   - " + p.getCodigo() + " | " + p.getDescripcion() +
                        " | stock=" + p.getStockActual() + " | $" + p.getPrecioVenta()));

        // 3) Buscar un producto específico
        Producto martillo = sp.buscarPorCodigo("HM-001")
                .orElseThrow(() -> new RuntimeException("HM-001 no existe"));
        int stockInicial = martillo.getStockActual();
        ok("Producto HM-001 encontrado, stock inicial = " + stockInicial);

        // 4) Registrar venta EFECTIVO de 2 unidades de HM-001
        Venta v = new Venta();
        v.setUsuarioId(u.getId());
        v.setFormaPago(Venta.FormaPago.EFECTIVO);
        v.getItems().add(new DetalleVenta(martillo, 2));

        ServicioVenta sv = new ServicioVenta();
        int ventaId = sv.registrar(v);
        ok("Venta EFECTIVO registrada, id=" + ventaId + ", total=$" + v.getTotal());

        Producto recargado = sp.buscarPorCodigo("HM-001").get();
        if (recargado.getStockActual() != stockInicial - 2) {
            fail("Stock no se descontó: antes=" + stockInicial + " después=" + recargado.getStockActual());
        }
        ok("Stock descontado correctamente: " + stockInicial + " → " + recargado.getStockActual());

        // 5) Venta a CTA_CTE a cliente habilitado (Carlos Fernández id=2)
        Producto tornillo = sp.buscarPorCodigo("BU-001").get();
        Venta v2 = new Venta();
        v2.setUsuarioId(u.getId());
        v2.setClienteId(2);
        v2.setFormaPago(Venta.FormaPago.CTA_CTE);
        v2.getItems().add(new DetalleVenta(tornillo, 10));
        int v2Id = sv.registrar(v2);
        ok("Venta CTA_CTE registrada, id=" + v2Id + ", total=$" + v2.getTotal());

        Cliente c = new ClienteDAO().buscarPorId(2).get();
        ok("Cliente id=2 saldo actualizado: $" + c.getSaldoCtaCte());

        // 6) Venta con stock insuficiente debe fallar
        Producto taladro = sp.buscarPorCodigo("HE-001").get();
        Venta v3 = new Venta();
        v3.setUsuarioId(u.getId());
        v3.setFormaPago(Venta.FormaPago.EFECTIVO);
        v3.getItems().add(new DetalleVenta(taladro, 9999));
        try {
            sv.registrar(v3);
            fail("La venta con stock insuficiente debería fallar");
        } catch (Exception ex) {
            ok("Venta con stock insuficiente rechazada: " + ex.getMessage());
        }

        System.out.println("\n=== Smoke test COMPLETADO OK ===");
    }

    private static void ok(String msg) { System.out.println("[OK]   " + msg); }
    private static void fail(String msg) { System.err.println("[FAIL] " + msg); System.exit(1); }
}
