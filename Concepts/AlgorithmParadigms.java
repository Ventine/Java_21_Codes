import java.util.Arrays;
import java.util.List;

/**
 * Ejemplos claros de tres paradigmas algorítmicos:
 *
 * 1. Programación Dinámica
 * 2. Greedy (Voraz)
 * 3. Backtracking
 *
 * Código educativo, explícito y directo.
 */
public class AlgorithmParadigms {

    /* =========================================================
       PROGRAMACIÓN DINÁMICA
       ========================================================= */

    /**
     * Cálculo de Fibonacci usando programación dinámica (bottom-up).
     *
     * Evita recomputaciones almacenando resultados previos.
     *
     * Complejidad:
     *  Tiempo: O(n)
     *  Espacio: O(n)
     */
    public static int fibonacciDP(int n) {
        if (n <= 1) return n;

        int[] dp = new int[n + 1];
        dp[0] = 0;
        dp[1] = 1;

        for (int i = 2; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        return dp[n];
    }

    /* =========================================================
       GREEDY
       ========================================================= */

    /**
     * Selección de actividades (interval scheduling).
     * Siempre se elige la actividad que termina antes.
     *
     * Supone que los intervalos están ordenados por tiempo de fin.
     *
     * Complejidad:
     *  Tiempo: O(n)
     *  Espacio: O(1)
     */
    public static int greedyActivitySelection(List<int[]> activities) {
        int count = 0;
        int lastEnd = -1;

        for (int[] act : activities) {
            if (act[0] >= lastEnd) {
                count++;
                lastEnd = act[1];
            }
        }
        return count;
    }

    /* =========================================================
       BACKTRACKING
       ========================================================= */

    /**
     * Problema clásico: N-Reinas.
     * Coloca N reinas en un tablero NxN sin que se ataquen.
     *
     * Complejidad:
     *  Exponencial (poda reduce el espacio de búsqueda).
     */
    public static class NQueens {
        private final int n;
        private final int[] board;
        private int solutions = 0;

        public NQueens(int n) {
            this.n = n;
            this.board = new int[n];
        }

        /**
         * Inicia el proceso de backtracking.
         */
        public int solve() {
            place(0);
            return solutions;
        }

        /**
         * Intenta colocar una reina en la fila actual.
         */
        private void place(int row) {
            if (row == n) {
                solutions++;
                return;
            }

            for (int col = 0; col < n; col++) {
                if (isSafe(row, col)) {
                    board[row] = col;
                    place(row + 1);
                }
            }
        }

        /**
         * Verifica que no haya conflictos verticales o diagonales.
         */
        private boolean isSafe(int row, int col) {
            for (int i = 0; i < row; i++) {
                int prevCol = board[i];
                if (prevCol == col) return false;
                if (Math.abs(prevCol - col) == row - i) return false;
            }
            return true;
        }
    }

    /* =========================================================
       DEMOSTRACIÓN
       ========================================================= */

    public static void main(String[] args) {

        // Programación dinámica
        int fib = fibonacciDP(10);

        // Greedy
        List<int[]> activities = Arrays.asList(
                new int[]{1, 3},
                new int[]{3, 5},
                new int[]{0, 6},
                new int[]{5, 7},
                new int[]{8, 9}
        );
        int maxActivities = greedyActivitySelection(activities);

        // Backtracking
        NQueens queens = new NQueens(8);
        int solutions = queens.solve();
    }
}
