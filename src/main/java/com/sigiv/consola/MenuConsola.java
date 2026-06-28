package com.sigiv.consola;

import com.sigiv.dao.ClienteDAO;
import com.sigiv.dao.ProveedorDAO;
import com.sigiv.dao.RubroDAO;
import com.sigiv.excepcion.EntidadNoEncontradaException;
import com.sigiv.excepcion.ValidacionException;
import com.sigiv.modelo.*;
import com.sigiv.servicio.ServicioAuth;
import com.sigiv.servicio.ServicioProducto;
import com.sigiv.servicio.ServicioVenta;
import com.sigiv.util.Algoritmos;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Interfaz de usuario por consola del sistema SIGIV-SM.
 *
 * <p>Es el componente central de la AP3: concentra la demostración de los
 * conceptos pedidos por la consigna:</p>
 * <ul>
 *   <li><b>Menú de selección</b> con {@code switch} y bucle {@code while}.</li>
 *   <li><b>Estructuras de control</b> condicionales y repetitivas.</li>
 *   <li><b>Manejo de excepciones</b> (checked y unchecked, multi-catch).</li>
 *   <li><b>Creación de objetos</b> y uso de <b>constructores</b>.</li>
 *   <li><b>Polimorfismo</b> sobre {@code List&lt;Persona&gt;} y comparadores.</li>
 *   <li><b>Algoritmos</b> de ordenación y búsqueda (clase {@code Algoritmos}).</li>
 * </ul>
 *
 * <p>Reutiliza los servicios y DAO ya existentes (arquitectura en 3 capas y
 * patrón DAO definidos en las entregas AP1/AP2).</p>
 */
public class MenuConsola {

    private final Scanner sc = new Scanner(System.in);
    private final ServicioAuth servicioAuth = new ServicioAuth();
    private final ServicioProducto servicioProducto = new ServicioProducto();
    private final ServicioVenta servicioVenta = new ServicioVenta();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final RubroDAO rubroDAO = new RubroDAO();

    private Usuario usuario; // sesión actual

    public static void main(String[] args) {
        new MenuConsola().iniciar();
    }

    public void iniciar() {
        System.out.println("==============================================");
        System.out.println("   SIGIV-SM | Ferretería San Martín (consola)");
        System.out.println("==============================================");

        if (!login()) {
            System.out.println("Demasiados intentos fallidos. El sistema se cierra.");
            return;
        }

        int opcion = -1;
        while (opcion != 0) {
            mostrarMenu();
            opcion = leerEntero("Opción");
            try {
                switch (opcion) {
                    case 1 -> listarProductos();
                    case 2 -> buscarProductoPorCodigo();
                    case 3 -> ordenarProductos();
                    case 4 -> productosBajoStock();
                    case 5 -> registrarVenta();
                    case 6 -> altaProducto();
                    case 7 -> agendaContactos();
                    case 8 -> reporteVentas();
                    case 0 -> System.out.println("Cerrando sesión. ¡Hasta luego!");
                    default -> System.out.println(">> Opción inexistente. Intente nuevamente.");
                }
            } catch (SQLException e) {
                // Error de infraestructura (base de datos)
                System.out.println("[ERROR BD] " + e.getMessage());
            } catch (ValidacionException e) {
                // Errores de negocio (stock, crédito, validaciones)
                System.out.println("[VALIDACIÓN] " + e.getMessage());
            }
            System.out.println();
        }
    }

    // ------------------------------------------------------------------
    //  Autenticación
    // ------------------------------------------------------------------
    private boolean login() {
        for (int intentos = 3; intentos > 0; intentos--) {
            System.out.println("\n--- Inicio de sesión ---");
            String user = leerTexto("Usuario");
            String pass = leerTexto("Contraseña");
            try {
                Optional<Usuario> o = servicioAuth.autenticar(user, pass);
                if (o.isPresent()) {
                    usuario = o.get();
                    System.out.println(">> Bienvenido/a, " + usuario.getNombreCompleto()
                            + " [" + usuario.getRol() + "]");
                    return true;
                }
                System.out.println(">> Credenciales inválidas. Intentos restantes: " + (intentos - 1));
            } catch (SQLException e) {
                System.out.println("[ERROR BD] No se pudo validar el usuario: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    private void mostrarMenu() {
        System.out.println("------------- MENÚ PRINCIPAL -------------");
        System.out.println(" 1) Listar productos");
        System.out.println(" 2) Buscar producto por código (búsqueda binaria)");
        System.out.println(" 3) Ordenar productos (QuickSort)");
        System.out.println(" 4) Productos bajo stock mínimo");
        System.out.println(" 5) Registrar venta");
        System.out.println(" 6) Alta de producto");
        System.out.println(" 7) Agenda de contactos (clientes y proveedores)");
        System.out.println(" 8) Reporte de ventas por forma de pago (exporta a archivo)");
        System.out.println(" 0) Salir");
        System.out.println("-----------------------------------------");
    }

    // ------------------------------------------------------------------
    //  Opción 1 - Listar productos
    // ------------------------------------------------------------------
    private void listarProductos() throws SQLException {
        List<Producto> productos = servicioProducto.listar();
        imprimirTablaProductos(productos);
    }

    // ------------------------------------------------------------------
    //  Opción 2 - Búsqueda binaria por código
    // ------------------------------------------------------------------
    private void buscarProductoPorCodigo() throws SQLException {
        String codigo = leerTexto("Código a buscar").toUpperCase();

        // 1) Se ordena la lista por código (precondición de la búsqueda binaria)
        List<Producto> ordenados = Algoritmos.quickSort(
                servicioProducto.listar(), Producto.POR_CODIGO);

        // 2) Clave de búsqueda: objeto "molde" con sólo el código cargado
        Producto clave = new Producto();
        clave.setCodigo(codigo);

        int idx = Algoritmos.busquedaBinaria(ordenados, clave, Producto.POR_CODIGO);
        if (idx >= 0) {
            System.out.println(">> Encontrado en la posición " + idx + " de la lista ordenada:");
            imprimirProducto(ordenados.get(idx));
        } else {
            // Demostración de excepción CHEQUEADA
            try {
                servicioProducto.obtenerPorCodigo(codigo);
            } catch (EntidadNoEncontradaException e) {
                System.out.println("[NO ENCONTRADO] " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------
    //  Opción 3 - Ordenar productos con QuickSort y comparadores
    // ------------------------------------------------------------------
    private void ordenarProductos() throws SQLException {
        System.out.println("Ordenar por:  1) Precio  2) Stock  3) Descripción");
        int criterio = leerEntero("Criterio");

        Comparator<Producto> cmp = switch (criterio) {
            case 1 -> Producto.POR_PRECIO;
            case 2 -> Producto.POR_STOCK;
            case 3 -> Producto.POR_DESCRIPCION;
            default -> throw new ValidacionException("Criterio inválido");
        };

        List<Producto> ordenados = Algoritmos.quickSort(servicioProducto.listar(), cmp);
        imprimirTablaProductos(ordenados);
    }

    // ------------------------------------------------------------------
    //  Opción 4 - Productos bajo stock mínimo (condicional + repetición)
    // ------------------------------------------------------------------
    private void productosBajoStock() throws SQLException {
        List<Producto> bajos = new ArrayList<>();
        for (Producto p : servicioProducto.listar()) {
            if (p.necesitaReposicion()) {
                bajos.add(p);
            }
        }
        if (bajos.isEmpty()) {
            System.out.println(">> No hay productos por debajo del stock mínimo.");
        } else {
            System.out.println(">> Productos que requieren reposición:");
            imprimirTablaProductos(bajos);
        }
    }

    // ------------------------------------------------------------------
    //  Opción 5 - Registrar venta
    // ------------------------------------------------------------------
    private void registrarVenta() throws SQLException {
        Venta venta = new Venta();                 // creación de objeto + constructor
        venta.setUsuarioId(usuario.getId());

        boolean agregando = true;
        while (agregando) {
            String codigo = leerTexto("Código de producto (ENTER para terminar)").toUpperCase();
            if (codigo.isBlank()) {
                agregando = false;
                continue;
            }
            Optional<Producto> op = servicioProducto.buscarPorCodigo(codigo);
            if (op.isEmpty()) {
                System.out.println("   Producto inexistente, intente otro.");
                continue;
            }
            Producto p = op.get();
            int cant = leerEntero("   Cantidad");
            venta.getItems().add(new DetalleVenta(p, cant)); // constructor con args
            System.out.printf("   + %d x %s (subtotal parcial $%s)%n",
                    cant, p.getDescripcion(), venta.getTotal());
        }

        if (venta.getItems().isEmpty()) {
            System.out.println(">> Venta cancelada (sin ítems).");
            return;
        }

        System.out.println("Forma de pago:  1) Efectivo  2) Transferencia  3) Cta. Cte.");
        int fp = leerEntero("Forma de pago");
        venta.setFormaPago(switch (fp) {
            case 2 -> Venta.FormaPago.TRANSFERENCIA;
            case 3 -> Venta.FormaPago.CTA_CTE;
            default -> Venta.FormaPago.EFECTIVO;
        });

        if (venta.getFormaPago() == Venta.FormaPago.CTA_CTE) {
            List<Cliente> clientes = clienteDAO.listar();
            for (Cliente c : clientes) {
                System.out.println("   id=" + c.getId() + " -> " + c.fichaResumen());
            }
            venta.setClienteId(leerEntero("Id de cliente"));
        }

        int id = servicioVenta.registrar(venta);
        System.out.printf(">> Venta N° %d registrada. Total: $%s%n", id, venta.getTotal());
    }

    // ------------------------------------------------------------------
    //  Opción 6 - Alta de producto
    // ------------------------------------------------------------------
    private void altaProducto() throws SQLException {
        if (usuario.getRol() != Rol.ADMIN) {
            throw new ValidacionException("Sólo un ADMIN puede dar de alta productos");
        }
        Producto p = new Producto();               // creación de objeto
        p.setCodigo(leerTexto("Código").toUpperCase());
        p.setDescripcion(leerTexto("Descripción"));

        System.out.println("Rubros disponibles:");
        for (Rubro r : rubroDAO.listar()) {
            System.out.println("   id=" + r.getId() + " -> " + r.getNombre());
        }
        p.setRubroId(leerEntero("Id de rubro"));
        p.setPrecioCosto(leerDecimal("Precio de costo"));
        p.setPrecioVenta(leerDecimal("Precio de venta"));
        p.setStockActual(leerEntero("Stock inicial"));
        p.setStockMinimo(leerEntero("Stock mínimo"));

        servicioProducto.crear(p);  // valida y puede lanzar ValidacionException
        System.out.println(">> Producto '" + p.getCodigo() + "' creado correctamente.");
    }

    // ------------------------------------------------------------------
    //  Opción 7 - Agenda polimórfica de contactos
    // ------------------------------------------------------------------
    private void agendaContactos() throws SQLException {
        // Lista heterogénea: clientes y proveedores tratados como Persona
        List<Persona> contactos = new ArrayList<>();
        contactos.addAll(clienteDAO.listar());
        contactos.addAll(proveedorDAO.listar());

        System.out.println(">> Agenda de contactos (" + contactos.size() + "):");
        for (Persona persona : contactos) {
            // POLIMORFISMO: tipoEntidad() y fichaResumen() se resuelven en
            // tiempo de ejecución según el objeto concreto.
            System.out.printf("   [%-9s] %s%n", persona.tipoEntidad(), persona.fichaResumen());
        }
    }

    // ------------------------------------------------------------------
    //  Opción 8 - Reporte de ventas por forma de pago
    //  (uso complementario de ArrayList + arreglo, y persistencia en archivo)
    // ------------------------------------------------------------------
    private void reporteVentas() throws SQLException {
        // 1) Datos dinámicos desde la BD: ArrayList de ventas
        List<Venta> ventas = servicioVenta.listar();
        if (ventas.isEmpty()) {
            System.out.println(">> No hay ventas confirmadas para reportar.");
            return;
        }

        // 2) Acumulación sobre un ARREGLO de tamaño fijo (una celda por forma de pago)
        Venta.FormaPago[] formas = Venta.FormaPago.values();
        BigDecimal[] totales = servicioVenta.totalesPorFormaPago(ventas);

        // 3) Presentación en la interfaz (consola)
        BigDecimal totalGeneral = BigDecimal.ZERO;
        StringBuilder reporte = new StringBuilder();
        reporte.append("REPORTE DE VENTAS POR FORMA DE PAGO%n".formatted());
        reporte.append("Generado: ")
               .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
               .append(String.format("%n"));
        reporte.append(String.format("Ventas confirmadas procesadas: %d%n", ventas.size()));
        reporte.append("-------------------------------------------%n".formatted());
        for (int i = 0; i < formas.length; i++) {           // recorrido del arreglo por índice
            reporte.append(String.format("%-16s $ %,12.2f%n", formas[i], totales[i]));
            totalGeneral = totalGeneral.add(totales[i]);
        }
        reporte.append("-------------------------------------------%n".formatted());
        reporte.append(String.format("%-16s $ %,12.2f%n", "TOTAL GENERAL", totalGeneral));

        System.out.println();
        System.out.print(reporte);

        // 4) Persistencia en archivo de texto (requisito opcional: manejo de archivos)
        exportarReporte(reporte.toString());
    }

    /** Guarda el reporte en un archivo .txt, manejando la posible {@link IOException}. */
    private void exportarReporte(String contenido) {
        try {
            Path dir = Paths.get("reportes");
            Files.createDirectories(dir);
            String nombre = "ventas-" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".txt";
            Path archivo = dir.resolve(nombre);
            try (BufferedWriter w = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
                w.write(contenido);
            }
            System.out.println(">> Reporte exportado a: " + archivo.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("[ERROR ARCHIVO] No se pudo guardar el reporte: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    //  Utilidades de impresión y de lectura robusta de entrada
    // ------------------------------------------------------------------
    private void imprimirTablaProductos(List<Producto> productos) {
        if (productos.isEmpty()) {
            System.out.println(">> Sin productos para mostrar.");
            return;
        }
        System.out.printf("%-10s %-32s %8s %6s%n", "CÓDIGO", "DESCRIPCIÓN", "PRECIO", "STOCK");
        System.out.println("-------------------------------------------------------------");
        for (Producto p : productos) {
            imprimirProducto(p);
        }
    }

    private void imprimirProducto(Producto p) {
        System.out.printf("%-10s %-32s %8s %6d%n",
                p.getCodigo(),
                acortar(p.getDescripcion(), 32),
                p.getPrecioVenta(),
                p.getStockActual());
    }

    private String acortar(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    /** Lee un entero validando el formato (maneja {@link NumberFormatException}). */
    private int leerEntero(String etiqueta) {
        while (true) {
            System.out.print(etiqueta + ": ");
            String linea = sc.nextLine().trim();
            try {
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                System.out.println("   Ingrese un número entero válido.");
            }
        }
    }

    /** Lee un decimal validando el formato. */
    private BigDecimal leerDecimal(String etiqueta) {
        while (true) {
            System.out.print(etiqueta + ": ");
            String linea = sc.nextLine().trim().replace(",", ".");
            try {
                return new BigDecimal(linea);
            } catch (NumberFormatException e) {
                System.out.println("   Ingrese un número válido (ej: 1500.50).");
            }
        }
    }

    private String leerTexto(String etiqueta) {
        System.out.print(etiqueta + ": ");
        return sc.nextLine().trim();
    }
}
