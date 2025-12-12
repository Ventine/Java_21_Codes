/**
 * Implementaciones de:
 * 1. Búsqueda binaria
 * 2. BFS (Breadth-First Search)
 * 3. DFS (Depth-First Search)
 *
 * Código directo, con comentarios y JavaDoc.
 */
public class SearchAlgorithms {

    /* =========================================================
       BÚSQUEDA BINARIA
       ========================================================= */

    /**
     * Realiza búsqueda binaria sobre un arreglo ordenado.
     *
     * @param arr   arreglo ordenado ascendentemente
     * @param target valor a buscar
     * @return índice del valor o -1 si no existe
     */
    public static int binarySearch(int[] arr, int target) {
        int low = 0;
        int high = arr.length - 1;

        // División repetida del intervalo en mitades
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int value = arr[mid];

            if (value == target) return mid;
            if (value < target) low = mid + 1;
            else high = mid - 1;
        }
        return -1;
    }

    /* =========================================================
       GRAFO PARA BFS / DFS
       ========================================================= */

    /**
     * Grafo simple dirigido usando lista de adyacencia.
     */
    public static class Graph {
        private final java.util.Map<String, java.util.List<String>> adj = new java.util.HashMap<>();

        /**
         * Agrega nodo si no existe.
         */
        private void ensure(String node) {
            adj.computeIfAbsent(node, k -> new java.util.ArrayList<>());
        }

        /**
         * Agrega arista dirigida u -> v.
         */
        public void addEdge(String u, String v) {
            ensure(u);
            ensure(v);
            adj.get(u).add(v);
        }

        /**
         * BFS desde un nodo inicial. Usa cola.
         *
         * @param start nodo inicial
         * @return orden de visita
         */
        public java.util.List<String> bfs(String start) {
            java.util.List<String> order = new java.util.ArrayList<>();
            java.util.Set<String> visited = new java.util.HashSet<>();
            java.util.ArrayDeque<String> queue = new java.util.ArrayDeque<>();

            queue.add(start);
            visited.add(start);

            while (!queue.isEmpty()) {
                String u = queue.remove();
                order.add(u);

                for (String v : adj.getOrDefault(u, java.util.List.of())) {
                    if (!visited.contains(v)) {
                        visited.add(v);
                        queue.add(v);
                    }
                }
            }
            return order;
        }

        /**
         * DFS desde un nodo inicial. Usa recursión.
         *
         * @param start nodo inicial
         * @return orden de visita
         */
        public java.util.List<String> dfs(String start) {
            java.util.List<String> order = new java.util.ArrayList<>();
            java.util.Set<String> visited = new java.util.HashSet<>();
            dfsRec(start, visited, order);
            return order;
        }

        private void dfsRec(String u, java.util.Set<String> visited, java.util.List<String> order) {
            visited.add(u);
            order.add(u);

            for (String v : adj.getOrDefault(u, java.util.List.of())) {
                if (!visited.contains(v)) dfsRec(v, visited, order);
            }
        }
    }

    /* =========================================================
       DEMOSTRACIÓN
       ========================================================= */

    public static void main(String[] args) {
        // Búsqueda binaria
        int[] sorted = {1, 3, 5, 7, 9, 11, 13};
        int idx = binarySearch(sorted, 7);

        // Grafo para BFS / DFS
        Graph g = new Graph();
        g.addEdge("A", "B");
        g.addEdge("A", "C");
        g.addEdge("B", "D");
        g.addEdge("C", "E");
        g.addEdge("E", "F");

        java.util.List<String> bfsOrder = g.bfs("A");
        java.util.List<String> dfsOrder = g.dfs("A");
    }
}
