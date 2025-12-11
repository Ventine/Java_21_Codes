/**
 * Ejemplos básicos en Java 21 de:
 * 1. Trie
 * 2. Grafo (lista de adyacencia)
 * 3. Heap (min-heap)
 *
 * Se minimiza el código para mostrar únicamente la estructura esencial.
 */
public class DataStructuresExamples {

    /* ============================================================
       =========================== TRIE ============================
       ============================================================ */

    /**
     * Nodo de un Trie. Cada nodo almacena enlaces a sus hijos (26 letras)
     * y una marca para indicar si termina una palabra.
     */
    static class TrieNode {
        TrieNode[] children = new TrieNode[26];
        boolean endOfWord;
    }

    /**
     * Implementación mínima de un Trie para palabras en minúsculas.
     */
    static class Trie {
        private final TrieNode root = new TrieNode();

        /**
         * Inserta una palabra carácter por carácter.
         */
        public void insert(String word) {
            TrieNode current = root;
            for (char c : word.toCharArray()) {
                int idx = c - 'a';
                if (current.children[idx] == null) {
                    current.children[idx] = new TrieNode();
                }
                current = current.children[idx];
            }
            current.endOfWord = true;
        }

        /**
         * Comprueba si la palabra existe exactamente.
         */
        public boolean search(String word) {
            TrieNode current = root;
            for (char c : word.toCharArray()) {
                int idx = c - 'a';
                if (current.children[idx] == null) return false;
                current = current.children[idx];
            }
            return current.endOfWord;
        }
    }


    /* ============================================================
       =========================== GRAFO ===========================
       ============================================================ */

    /**
     * Grafo dirigido usando lista de adyacencia.
     */
    static class Graph {
        private final java.util.Map<Integer, java.util.List<Integer>> adj = new java.util.HashMap<>();

        /**
         * Agrega un nodo y sus lista inicial de vecinos si no existe.
         */
        private void ensureVertex(int v) {
            adj.putIfAbsent(v, new java.util.ArrayList<>());
        }

        /**
         * Crea una arista dirigida v -> w.
         */
        public void addEdge(int v, int w) {
            ensureVertex(v);
            ensureVertex(w);
            adj.get(v).add(w);
        }

        /**
         * Búsqueda en profundidad (DFS).
         */
        public void dfs(int start) {
            java.util.Set<Integer> visited = new java.util.HashSet<>();
            dfsRec(start, visited);
        }

        private void dfsRec(int v, java.util.Set<Integer> visited) {
            if (!visited.add(v)) return;
            for (int w : adj.get(v)) {
                dfsRec(w, visited);
            }
        }
    }


    /* ============================================================
       =========================== HEAP ============================
       ============================================================ */

    /**
     * Min-heap clásico usando array dinámico.
     *
     * Propiedad: raíz es siempre el mínimo.
     */
    static class MinHeap {
        private final java.util.List<Integer> heap = new java.util.ArrayList<>();

        /**
         * Inserta un valor y reordena para mantener el min-heap.
         */
        public void push(int value) {
            heap.add(value);
            siftUp(heap.size() - 1);
        }

        /**
         * Extrae el mínimo.
         */
        public int pop() {
            if (heap.isEmpty()) throw new IllegalStateException("Heap vacío");
            int min = heap.getFirst();
            int last = heap.removeLast();
            if (!heap.isEmpty()) {
                heap.set(0, last);
                siftDown(0);
            }
            return min;
        }

        private void siftUp(int i) {
            while (i > 0) {
                int parent = (i - 1) / 2;
                if (heap.get(i) >= heap.get(parent)) break;
                swap(i, parent);
                i = parent;
            }
        }

        private void siftDown(int i) {
            int size = heap.size();
            while (true) {
                int left = i * 2 + 1;
                int right = i * 2 + 2;
                int smallest = i;

                if (left < size && heap.get(left) < heap.get(smallest)) smallest = left;
                if (right < size && heap.get(right) < heap.get(smallest)) smallest = right;
                if (smallest == i) break;

                swap(i, smallest);
                i = smallest;
            }
        }

        private void swap(int a, int b) {
            int tmp = heap.get(a);
            heap.set(a, heap.get(b));
            heap.set(b, tmp);
        }
    }


    /* ============================================================
       =========================== MAIN ============================
       ============================================================ */

    /**
     * Demostración mínima.
     */
    public static void main(String[] args) {
        Trie trie = new Trie();
        trie.insert("hola");
        trie.insert("holaque");
        trie.search("hola");

        Graph g = new Graph();
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.dfs(1);

        MinHeap h = new MinHeap();
        h.push(5);
        h.push(2);
        h.push(9);
        h.pop();
    }
}
