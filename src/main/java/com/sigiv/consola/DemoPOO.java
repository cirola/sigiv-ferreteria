package com.sigiv.consola;

import com.sigiv.excepcion.EntidadNoEncontradaException;
import com.sigiv.excepcion.StockInsuficienteException;
import com.sigiv.modelo.*;
import com.sigiv.util.Algoritmos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Demostración autónoma (sin base de datos) de los cuatro pilares de la POO y
 * de los algoritmos de ordenación/búsqueda. Sirve como evidencia ejecutable
 * para el informe AP3 y para verificar el comportamiento sin depender de MySQL.
 *
 * <p>Ejecutar: {@code java -cp target/classes com.sigiv.consola.DemoPOO}</p>
 */
public class DemoPOO {

    public static void main(String[] args) {
        System.out.println("===== DEMO POO - SIGIV-SM =====\n");

        demostrarHerenciaYPolimorfismo();
        demostrarAlgoritmos();
        demostrarExcepciones();

        System.out.println("\n===== FIN DEMO =====");
    }

    /** ABSTRACCIÓN + HERENCIA + POLIMORFISMO + ENCAPSULAMIENTO. */
    private static void demostrarHerenciaYPolimorfismo() {
        System.out.println("--- 1) Herencia y polimorfismo (Persona) ---");

        // Creación de objetos con constructores; tipos distintos, base común.
        Persona cliente = new Cliente(1, "María González", "27-33444555-6",
                "351-1112233", "maria@mail.com", "Av. Colón 1200",
                true, new BigDecimal("80000"), new BigDecimal("15000"));
        Persona proveedor = new Proveedor(1, "Distribuidora Central SA",
                "30-12345678-9", "351-4445566", "ventas@dc.com.ar", "Ruta 9 Km 5");

        List<Persona> contactos = new ArrayList<>();
        contactos.add(cliente);
        contactos.add(proveedor);

        // La misma llamada produce comportamientos distintos (polimorfismo).
        for (Persona p : contactos) {
            System.out.printf("  [%s] %s%n", p.tipoEntidad(), p.fichaResumen());
        }
        System.out.println();
    }

    /** Algoritmos genéricos de ordenación y búsqueda. */
    private static void demostrarAlgoritmos() {
        System.out.println("--- 2) Ordenación (QuickSort) y búsqueda binaria ---");

        List<Producto> productos = catalogoDemo();

        List<Producto> porPrecio = Algoritmos.quickSort(productos, Producto.POR_PRECIO);
        System.out.println("  Ordenado por precio:");
        porPrecio.forEach(p -> System.out.printf("    %-22s $%s%n",
                p.getDescripcion(), p.getPrecioVenta()));

        List<Producto> porCodigo = Algoritmos.quickSort(productos, Producto.POR_CODIGO);
        Producto clave = new Producto();
        clave.setCodigo("HM-001");
        int idx = Algoritmos.busquedaBinaria(porCodigo, clave, Producto.POR_CODIGO);
        System.out.println("  Búsqueda binaria de HM-001 -> índice " + idx
                + " (" + porCodigo.get(idx).getDescripcion() + ")");
        System.out.println();
    }

    /** Manejo de excepciones chequeadas y no chequeadas. */
    private static void demostrarExcepciones() {
        System.out.println("--- 3) Manejo de excepciones ---");

        // No chequeada (de negocio)
        try {
            throw new StockInsuficienteException("HE-001", 10, 3);
        } catch (StockInsuficienteException e) {
            System.out.println("  [unchecked] " + e.getMessage());
        }

        // Chequeada (el compilador obliga a manejarla)
        try {
            buscarObligatorio("XXX-999");
        } catch (EntidadNoEncontradaException e) {
            System.out.println("  [checked]   " + e.getMessage());
        }
    }

    private static void buscarObligatorio(String codigo) throws EntidadNoEncontradaException {
        throw new EntidadNoEncontradaException("No existe el producto '" + codigo + "'");
    }

    private static List<Producto> catalogoDemo() {
        List<Producto> l = new ArrayList<>();
        l.add(crear("HM-001", "Martillo carpintero", "4000", 9));
        l.add(crear("BU-001", "Tornillo 3\"", "50", 1970));
        l.add(crear("HE-001", "Taladro percutor", "55000", 3));
        l.add(crear("EL-003", "Lámpara LED 9W", "1900", 60));
        return l;
    }

    private static Producto crear(String codigo, String desc, String precio, int stock) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setDescripcion(desc);
        p.setPrecioVenta(new BigDecimal(precio));
        p.setStockActual(stock);
        return p;
    }
}
