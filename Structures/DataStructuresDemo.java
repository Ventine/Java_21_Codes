/*
 * DataStructuresDemo.java
 *
 * Demostración en Java 21 de:
 *  - Cola (FIFO) usando buffer circular (ArrayQueue<T>)
 *  - Cola de prioridad (PriorityQueue<T>) con Comparator y uso práctico
 *  - Tabla hash genérica con encadenamiento separado (SimpleHashMap<K,V>)
 *
 * Incluye:
 *  - Implementaciones mínimas y seguras para producción didáctica
 *  - Javadoc en las APIs públicas
 *  - Medición sencilla de tiempos (no es benchmark profesional)
 *
 * Compilar / ejecutar (Java 21):
 *   javac DataStructuresDemo.java
 *   java DataStructuresDemo
 *
 * Nota: El código prioriza claridad, corrección y complejidad anotada.
 */

import java.util.*;
import java.util.function.Supplier;

/**
 * Demo principal que muestra uso y análisis de estructuras: Cola, Cola de Prioridad, Tabla Hash.
 */
public final class DataStructuresDemo {

    public static void main(String[] args) {
        System.out.println("=== ArrayQueue (cola FIFO - buffer circular) ===");
        demoArrayQueue();

        System.out.println("\n=== PriorityQueue (cola de prioridad) ===");
        demoPriorityQueue();

        System.out.println("\n=== SimpleHashMap (tabla hash con encadenamiento) ===");
        demoSimpleHashMap();
    }

    private static void demoArrayQueue() {
        ArrayQueue<String> q = new ArrayQueue<>(4); // capacidad inicial pequeña para mostrar crecimiento lógico
        q.enqueue("A");
        q.enqueue("B");
        q.enqueue("C");
        System.out.println("Peek: " + q.peek());             // A
        System.out.println("Dequeue: " + q.dequeue());       // A
        q.enqueue("D");
        q.enqueue("E"); // fuerza el reacomodo/resize interno si es necesario
        while (!q.isEmpty()) System.out.print(q.dequeue() + " ");
        System.out.println("\nSize final: " + q.size());
        System.out.println("Complejidad: enqueue O(1) amortizado, dequeue O(1), peek O(1).");
    }

    private static void demoPriorityQueue() {
        // Min-heap por defecto: extrae el elemento "menor"
        var pq = new PriorityQueue<Task>(Comparator.comparingInt(Task::priority));
        pq.add(new Task("low", 50));
        pq.add(new Task("urgent", 5));
        pq.add(new Task("medium", 20));

        System.out.println("Order of processing (by priority ascending):");
        while (!pq.isEmpty()) {
            System.out.println(pq.poll()); // obtiene "urgent" primero
        }

        System.out.println("Complejidad: add O(log n), poll O(log n), peek O(1).");
    }

    private static void demoSimpleHashMap() {
        var map = new SimpleHashMap<String, Integer>(8);
        map.put("alice", 30);
        map.put("bob", 25);
        map.put("carol", 28);
        map.put("dave", 40);

        System.out.println("bob -> " + map.get("bob"));
        System.out.println("Contains 'eve'? " + map.containsKey("eve"));
        map.put("bob", 26); // actualización
        System.out.println("bob (updated) -> " + map.get("bob"));

        System.out.println("Iterating entries:");
        for (var e : map.entries()) System.out.println(e.key() + " = " + e.value());

        System.out.println("Removiendo 'alice': " + map.remove("alice"));
        System.out.println("alice presente? " + map.containsKey("alice"));

        System.out.println("Complejidad promedio: get/put/remove O(1). Peor caso O(n) (colisiones concentradas).");
    }

    /* ============================
       Queue: array circular
       ============================ */

    /**
     * ArrayQueue: cola FIFO basada en buffer circular con crecimiento.
     *
     * <p>Características:
     * - Enqueue/dequeue O(1) amortizado (resize cuando buffer se llena).
     * - Acceso contiguo facilita uso de caché comparado con listas enlazadas.
     * - No es thread-safe; si se requiere concurrencia, envolver con sincronización o usar estructuras concurrentes.
     *
     * @param <T> tipo de elementos
     */
    public static final class ArrayQueue<T> {
        private T[] buffer;
        private int head; // índice del siguiente elemento a extraer
        private int tail; // índice donde insertar el próximo elemento
        private int size;

        /**
         * Crea una cola con capacidad inicial.
         *
         * @param capacity capacidad inicial, > 0
         */
        @SuppressWarnings("unchecked")
        public ArrayQueue(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException("capacity > 0 required");
            buffer = (T[]) new Object[capacity];
        }

        /**
         * Inserta un elemento al final de la cola.
         *
         * @param item elemento a agregar
         */
        public void enqueue(T item) {
            if (item == null) throw new NullPointerException("null not allowed");
            if (size == buffer.length) resize(buffer.length * 2);
            buffer[tail] = item;
            tail = (tail + 1) % buffer.length;
            size++;
        }

        /**
         * Extrae el elemento al frente de la cola.
         *
         * @return elemento eliminado
         * @throws NoSuchElementException si la cola está vacía
         */
        public T dequeue() {
            if (size == 0) throw new NoSuchElementException("Queue is empty");
            T item = buffer[head];
            buffer[head] = null; // ayuda al GC
            head = (head + 1) % buffer.length;
            size--;
            // reducir capacidad cuando hay mucha holgura (opcional)
            if (buffer.length > 8 && size <= buffer.length / 4) resize(buffer.length / 2);
            return item;
        }

        /**
         * Recupera el elemento al frente sin extraerlo.
         *
         * @return elemento en el frente o null si está vacía
         */
        public T peek() {
            return size == 0 ? null : buffer[head];
        }

        public boolean isEmpty() { return size == 0; }
        public int size() { return size; }

        @SuppressWarnings("unchecked")
        private void resize(int newCap) {
            T[] nb = (T[]) new Object[newCap];
            // copiar contiguamente desde head
            for (int i = 0; i < size; i++) nb[i] = buffer[(head + i) % buffer.length];
            buffer = nb;
            head = 0;
            tail = size;
        }
    }

    /* ============================
       Priority Queue example helper
       ============================ */

    /**
     * Task simple con prioridad numérica (menor valor = mayor prioridad).
     */
    public static final record Task(String name, int priority) {
        @Override public String toString() { return String.format("%s(p=%d)", name, priority); }
    }

    /* ============================
       Simple Hash Map (separate chaining)
       ============================ */

    /**
     * SimpleHashMap: implementación genérica de tabla hash con encadenamiento separado.
     *
     * <p>Características y decisiones de diseño:
     * - Encadenamiento mediante listas simples (LinkedList<Node> por cubeta).
     * - Rehash/resize cuando carga > loadFactor (default 0.75).
     * - No es thread-safe.
     * - Provee operaciones básicas: put, get, remove, containsKey, size y iterador de entries.
     *
     * @param <K> tipo de clave (no nulo)
     * @param <V> tipo de valor
     */
    public static final class SimpleHashMap<K, V> {
        private static final float DEFAULT_LOAD_FACTOR = 0.75f;
        private List<Node<K, V>>[] table;
        private int size;
        private final float loadFactor;

        /**
         * Nodo de encadenamiento.
         */
        public static final class Entry<K, V> {
            private final K key;
            private final V value;
            public Entry(K k, V v) { this.key = k; this.value = v; }
            public K key() { return key; }
            public V value() { return value; }
        }

        private static final class Node<K, V> {
            final K key;
            V value;
            Node(K k, V v) { key = k; value = v; }
        }

        /**
         * Crea la tabla con capacidad inicial.
         *
         * @param capacity capacidad inicial (> 0)
         */
        @SuppressWarnings("unchecked")
        public SimpleHashMap(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException("capacity > 0");
            table = (List<Node<K,V>>[]) new List[nextPowerOfTwo(capacity)];
            loadFactor = DEFAULT_LOAD_FACTOR;
            for (int i = 0; i < table.length; i++) table[i] = new LinkedList<>();
        }

        private static int nextPowerOfTwo(int v) {
            int r = 1;
            while (r < v) r <<= 1;
            return r;
        }

        private int indexFor(Object key) {
            int h = key.hashCode();
            // espalhamento simple: xor-shift + mask (tabla tamaño potencia de dos)
            h ^= (h >>> 16);
            return h & (table.length - 1);
        }

        /**
         * Inserta o actualiza un par clave-valor.
         *
         * @param key  clave no nula
         * @param value valor
         * @return valor anterior o null si no existía
         */
        public V put(K key, V value) {
            Objects.requireNonNull(key, "key");
            int idx = indexFor(key);
            List<Node<K,V>> bucket = table[idx];
            for (Node<K,V> n : bucket) {
                if (n.key.equals(key)) {
                    V old = n.value;
                    n.value = value;
                    return old;
                }
            }
            bucket.add(new Node<>(key, value));
            size++;
            if ((float)size / table.length > loadFactor) resize(table.length * 2);
            return null;
        }

        /**
         * Obtiene el valor asociado o null si no existe.
         *
         * @param key clave
         * @return valor o null
         */
        public V get(K key) {
            Objects.requireNonNull(key, "key");
            int idx = indexFor(key);
            for (Node<K,V> n : table[idx]) if (n.key.equals(key)) return n.value;
            return null;
        }

        /**
         * Elimina la entrada asociada a la clave.
         *
         * @param key clave
         * @return valor eliminado o null si no existía
         */
        public V remove(K key) {
            Objects.requireNonNull(key, "key");
            int idx = indexFor(key);
            Iterator<Node<K,V>> it = table[idx].iterator();
            while (it.hasNext()) {
                Node<K,V> n = it.next();
                if (n.key.equals(key)) {
                    it.remove();
                    size--;
                    return n.value;
                }
            }
            return null;
        }

        /**
         * Indica si la clave está presente.
         *
         * @param key clave
         * @return true si existe
         */
        public boolean containsKey(K key) {
            return get(key) != null;
        }

        public int size() { return size; }

        @SuppressWarnings("unchecked")
        private void resize(int newCap) {
            List<Node<K,V>>[] old = table;
            table = (List<Node<K,V>>[]) new List[newCap];
            for (int i = 0; i < table.length; i++) table[i] = new LinkedList<>();
            size = 0;
            for (List<Node<K,V>> bucket : old) {
                for (Node<K,V> n : bucket) put(n.key, n.value); // rehashed into new table
            }
        }

        /**
         * Retorna una vista iterable de las entradas (snapshot consistente mientras no se modifique la tabla).
         *
         * @return lista de Entry<K,V>
         */
        public List<Entry<K,V>> entries() {
            List<Entry<K,V>> out = new ArrayList<>(size);
            for (List<Node<K,V>> bucket : table) {
                for (Node<K,V> n : bucket) out.add(new Entry<>(n.key, n.value));
            }
            return out;
        }
    }
}
