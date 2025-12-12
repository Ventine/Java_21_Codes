/**
 * Demostración de estructuras de datos básicas en Java 21:
 * - Array
 * - Lista enlazada simple
 * - Pila
 *
 * Cada estructura incluye implementación mínima y uso.
 */
public class EstructurasDatosDemo {

    public static void main(String[] args) {
        usarArray();
        usarListaEnlazada();
        usarPila();
    }

    /**
     * Demostración del uso de un Array.
     * Un array posee tamaño fijo y permite acceso O(1) por índice.
     */
    public static void usarArray() {
        // Declaración y creación de un array de enteros de tamaño fijo
        int[] numeros = new int[5];

        // Asignación de valores
        numeros[0] = 10;
        numeros[1] = 20;
        numeros[2] = 30;
        numeros[3] = 40;
        numeros[4] = 50;

        // Acceso directo por índice
        int valor = numeros[2];  // O(1)

        System.out.println("Array: valor en índice 2 = " + valor);
    }

    /**
     * Demostración de una lista enlazada simple.
     * Cada nodo contiene un valor y un enlace al siguiente nodo.
     * Inserciones al inicio O(1).
     */
    public static void usarListaEnlazada() {
        ListaEnlazadaSimple lista = new ListaEnlazadaSimple();
        lista.agregarInicio(30);
        lista.agregarInicio(20);
        lista.agregarInicio(10);

        System.out.println("Lista enlazada: ");
        lista.imprimir();
    }

    /**
     * Demostración de una pila.
     * La pila sigue la regla LIFO.
     * push y pop se realizan en O(1).
     */
    public static void usarPila() {
        Pila pila = new Pila();
        pila.push(100);
        pila.push(200);
        pila.push(300);

        System.out.println("Pila pop: " + pila.pop());
        System.out.println("Pila pop: " + pila.pop());
        System.out.println("Pila pop: " + pila.pop());
    }
}

/**
 * Implementación de una lista enlazada simple.
 * Solo mantiene referencia al primer nodo.
 */
class ListaEnlazadaSimple {

    /**
     * Nodo interno que almacena valor y referencia al siguiente nodo.
     */
    private static class Nodo {
        int valor;
        Nodo siguiente;

        Nodo(int valor) {
            this.valor = valor;
        }
    }

    private Nodo cabeza; // Primer nodo de la lista

    /**
     * Inserta un nuevo valor al inicio de la lista.
     * Operación O(1).
     *
     * @param valor valor a insertar
     */
    public void agregarInicio(int valor) {
        Nodo nuevo = new Nodo(valor);
        nuevo.siguiente = cabeza;
        cabeza = nuevo;
    }

    /**
     * Recorre e imprime todos los valores de la lista.
     * Operación O(n).
     */
    public void imprimir() {
        Nodo actual = cabeza;
        while (actual != null) {
            System.out.println(actual.valor);
            actual = actual.siguiente;
        }
    }
}

/**
 * Implementación de una pila basada en nodos.
 * push y pop en O(1).
 */
class Pila {

    /**
     * Nodo para almacenar cada valor de la pila.
     */
    private static class Nodo {
        int valor;
        Nodo siguiente;

        Nodo(int valor) {
            this.valor = valor;
        }
    }

    private Nodo tope; // Último elemento insertado (LIFO)

    /**
     * Inserta un elemento en el tope de la pila.
     *
     * @param valor valor a insertar
     */
    public void push(int valor) {
        Nodo nuevo = new Nodo(valor);
        nuevo.siguiente = tope;
        tope = nuevo;
    }

    /**
     * Extrae y retorna el último elemento insertado.
     *
     * @return valor extraído
     */
    public int pop() {
        if (tope == null) {
            throw new IllegalStateException("Pila vacía");
        }
        int valor = tope.valor;
        tope = tope.siguiente;
        return valor;
    }
}
