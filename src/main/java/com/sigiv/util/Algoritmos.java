package com.sigiv.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implementaciones <b>propias</b> (no delegadas a {@code Collections.sort}) de
 * algoritmos clásicos de ordenación y búsqueda, escritas con <b>genéricos</b>
 * ({@code <T>}) y {@link Comparator} para que funcionen con cualquier tipo de
 * objeto. El uso de {@code Comparator} es una forma de <b>polimorfismo</b>: el
 * mismo algoritmo ordena por el criterio que se le pase.
 *
 * <p>Se incluyen para cumplir el requisito (opcional) del TP de aplicar
 * algoritmos de ordenación y búsqueda, y para evidenciar el manejo de
 * estructuras de repetición y condicionales.</p>
 */
public final class Algoritmos {

    private Algoritmos() {
    }

    // ------------------------------------------------------------------
    //  ORDENACIÓN
    // ------------------------------------------------------------------

    /**
     * Ordena la lista con el algoritmo <b>QuickSort</b> (O(n log n) promedio).
     * Devuelve una copia ordenada; no modifica la lista original.
     */
    public static <T> List<T> quickSort(List<T> original, Comparator<T> cmp) {
        List<T> lista = new ArrayList<>(original);
        quickSort(lista, 0, lista.size() - 1, cmp);
        return lista;
    }

    private static <T> void quickSort(List<T> a, int desde, int hasta, Comparator<T> cmp) {
        if (desde >= hasta) {
            return;
        }
        int p = particionar(a, desde, hasta, cmp);
        quickSort(a, desde, p - 1, cmp);
        quickSort(a, p + 1, hasta, cmp);
    }

    private static <T> int particionar(List<T> a, int desde, int hasta, Comparator<T> cmp) {
        T pivote = a.get(hasta);
        int i = desde - 1;
        for (int j = desde; j < hasta; j++) {
            if (cmp.compare(a.get(j), pivote) <= 0) {
                i++;
                intercambiar(a, i, j);
            }
        }
        intercambiar(a, i + 1, hasta);
        return i + 1;
    }

    /**
     * Ordena la lista con el algoritmo <b>BubbleSort</b> (O(n²)). Se incluye con
     * fines didácticos para contrastar con QuickSort.
     */
    public static <T> List<T> bubbleSort(List<T> original, Comparator<T> cmp) {
        List<T> a = new ArrayList<>(original);
        int n = a.size();
        for (int i = 0; i < n - 1; i++) {
            boolean huboCambio = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (cmp.compare(a.get(j), a.get(j + 1)) > 0) {
                    intercambiar(a, j, j + 1);
                    huboCambio = true;
                }
            }
            if (!huboCambio) {
                break; // ya está ordenada
            }
        }
        return a;
    }

    private static <T> void intercambiar(List<T> a, int i, int j) {
        T tmp = a.get(i);
        a.set(i, a.get(j));
        a.set(j, tmp);
    }

    // ------------------------------------------------------------------
    //  BÚSQUEDA
    // ------------------------------------------------------------------

    /**
     * <b>Búsqueda binaria</b> (O(log n)). REQUIERE que la lista esté ordenada
     * según el mismo {@code cmp}. Devuelve el índice del elemento o -1.
     */
    public static <T> int busquedaBinaria(List<T> ordenada, T clave, Comparator<T> cmp) {
        int desde = 0;
        int hasta = ordenada.size() - 1;
        while (desde <= hasta) {
            int medio = (desde + hasta) >>> 1; // división entera segura
            int c = cmp.compare(ordenada.get(medio), clave);
            if (c == 0) {
                return medio;
            } else if (c < 0) {
                desde = medio + 1;
            } else {
                hasta = medio - 1;
            }
        }
        return -1;
    }

    /**
     * <b>Búsqueda lineal</b> (O(n)). No requiere orden previo. Devuelve el
     * índice del primer elemento que cumple el predicado de igualdad, o -1.
     */
    public static <T> int busquedaLineal(List<T> lista, T clave, Comparator<T> cmp) {
        for (int i = 0; i < lista.size(); i++) {
            if (cmp.compare(lista.get(i), clave) == 0) {
                return i;
            }
        }
        return -1;
    }
}
