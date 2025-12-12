import java.util.*;

/**
 * Implementaciones claras de:
 * 1. Dijkstra
 * 2. A* (A estrella)
 * 3. Bellman-Ford
 *
 * Grafo con pesos, recorrido, heurística y manejo de distancias.
 */
public class PathfindingAlgorithms {

    /* =========================================================
       ESTRUCTURA DE GRAFO PESADO
       ========================================================= */

    /**
     * Grafo dirigido y ponderado usando listas de adyacencia.
     */
    public static class WeightedGraph {
        private final Map<String, List<Edge>> adj = new HashMap<>();

        /**
         * Representa una arista u -> v con peso w.
         */
        public record Edge(String to, int weight) {}

        /**
         * Garantiza existencia del nodo.
         */
        private void ensure(String node) {
            adj.computeIfAbsent(node, k -> new ArrayList<>());
        }

        /**
         * Agrega arista dirigida con peso.
         */
        public void addEdge(String from, String to, int weight) {
            ensure(from);
            ensure(to);
            adj.get(from).add(new Edge(to, weight));
        }

        /**
         * Retorna lista de aristas desde un nodo.
         */
        public List<Edge> neighbors(String node) {
            return adj.getOrDefault(node, List.of());
        }

        /**
         * Retorna nodos presentes en el grafo.
         */
        public Set<String> nodes() {
            return adj.keySet();
        }
    }

    /* =========================================================
       DIJKSTRA
       ========================================================= */

    /**
     * Dijkstra: distancias mínimas con pesos no negativos.
     *
     * @param g grafo
     * @param start nodo origen
     * @return mapa nodo -> distancia mínima
     */
    public static Map<String, Integer> dijkstra(WeightedGraph g, String start) {
        Map<String, Integer> dist = new HashMap<>();
        for (String node : g.nodes()) dist.put(node, Integer.MAX_VALUE);
        dist.put(start, 0);

        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));
        pq.add(Map.entry(start, 0));

        while (!pq.isEmpty()) {
            var current = pq.poll();
            String u = current.getKey();
            int d = current.getValue();

            if (d > dist.get(u)) continue;

            for (var e : g.neighbors(u)) {
                int nd = d + e.weight();
                if (nd < dist.get(e.to())) {
                    dist.put(e.to(), nd);
                    pq.add(Map.entry(e.to(), nd));
                }
            }
        }
        return dist;
    }

    /* =========================================================
       A* (A ESTRELLA)
       ========================================================= */

    /**
     * A*: usa g(n) + h(n). Requiere heurística admisible.
     *
     * @param g grafo
     * @param start origen
     * @param goal objetivo
     * @param h heurística h(n)
     * @return distancia mínima estimada o Integer.MAX_VALUE si no hay camino
     */
    public static int aStar(WeightedGraph g, String start, String goal, Map<String, Integer> h) {
        Map<String, Integer> gScore = new HashMap<>();
        for (String node : g.nodes()) gScore.put(node, Integer.MAX_VALUE);
        gScore.put(start, 0);

        PriorityQueue<String> open = new PriorityQueue<>(
                Comparator.comparingInt(n -> gScore.get(n) + h.getOrDefault(n, 0))
        );
        open.add(start);

        Set<String> closed = new HashSet<>();

        while (!open.isEmpty()) {
            String u = open.poll();
            if (u.equals(goal)) return gScore.get(u);
            closed.add(u);

            for (var e : g.neighbors(u)) {
                if (closed.contains(e.to())) continue;

                int tentative = gScore.get(u) + e.weight();
                if (tentative < gScore.get(e.to())) {
                    gScore.put(e.to(), tentative);
                    open.add(e.to());
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    /* =========================================================
       BELLMAN-FORD
       ========================================================= */

    /**
     * Bellman-Ford: permite pesos negativos.
     * Detecta ciclos negativos.
     *
     * @param g grafo
     * @param start origen
     * @return mapa nodo -> distancia mínima
     * @throws IllegalStateException si existe ciclo negativo
     */
    public static Map<String, Integer> bellmanFord(WeightedGraph g, String start) {
        Map<String, Integer> dist = new HashMap<>();
        for (String node : g.nodes()) dist.put(node, Integer.MAX_VALUE);
        dist.put(start, 0);

        int V = g.nodes().size();

        for (int i = 0; i < V - 1; i++) {
            boolean updated = false;
            for (String u : g.nodes()) {
                int du = dist.get(u);
                if (du == Integer.MAX_VALUE) continue;

                for (var e : g.neighbors(u)) {
                    int nd = du + e.weight();
                    if (nd < dist.get(e.to())) {
                        dist.put(e.to(), nd);
                        updated = true;
                    }
                }
            }
            if (!updated) break;
        }

        for (String u : g.nodes()) {
            int du = dist.get(u);
            if (du == Integer.MAX_VALUE) continue;

            for (var e : g.neighbors(u)) {
                int nd = du + e.weight();
                if (nd < dist.get(e.to())) {
                    throw new IllegalStateException("Ciclo negativo detectado");
                }
            }
        }

        return dist;
    }

    /* =========================================================
       DEMOSTRACIÓN
       ========================================================= */

    public static void main(String[] args) {
        WeightedGraph g = new WeightedGraph();
        g.addEdge("A", "B", 2);
        g.addEdge("A", "C", 5);
        g.addEdge("B", "D", 1);
        g.addEdge("C", "D", 2);
        g.addEdge("D", "E", 3);

        Map<String, Integer> h = Map.of(
                "A", 6,
                "B", 4,
                "C", 2,
                "D", 1,
                "E", 0
        );

        var d1 = dijkstra(g, "A");
        var d2 = aStar(g, "A", "E", h);
        var d3 = bellmanFord(g, "A");
    }
}
