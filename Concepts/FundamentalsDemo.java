/**
 * FundamentalsDemo.java
 *
 * Requisitos aplicados:
 * - Fundamentos de computación: entrada-transformación-salida, modelado con tipos.
 * - Representación binaria: conversión explícita, complemento a dos, IEEE-754.
 * - Complejidad temporal y espacial: ejemplo con Criba de Eratóstenes (análisis).
 * - Uso de características modernas de Java 21: record, virtual threads.
 *
 * Compilar / Ejecutar (Java 21):
 *   javac --enable-preview --release 21 FundamentalsDemo.java
 *   java --enable-preview FundamentalsDemo 100000
 *
 * Argumento opcional: un entero n para generar primos hasta n (por defecto 100).
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public final class FundamentalsDemo {

    /**
     * Resultado empaquetado de una operación con medición simple.
     */
    public record Measured<T>(T result, long timeNs, long memoryBytes) { }

    public static void main(String[] args) throws Exception {
        int n = 100;
        if (args.length > 0) {
            try { n = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }

        // 1) Representación binaria - sincronamente y con virtual threads para mostrar concurrencia ligera.
        List<Integer> examples = List.of(0, 1, -1, 13, -13, 1024, -1024);
        System.out.println("=== Representación binaria (ejemplos) ===");
        // lanzar conversiones en virtual threads para mostrar modelo concurrente escalable
        List<Thread> threads = new ArrayList<>();
        for (int v : examples) {
            Thread t = Thread.ofVirtual().unstarted(() -> {
                String bin = toBinaryString(v);
                String twos = toTwosComplement(v, 32);
                String ieee = doubleToIEEE754((double) v);
                System.out.printf("n=%d -> bin=%s, two's=%s, IEEE754(double)=%s%n", v, bin, twos, ieee);
            });
            threads.add(t);
            t.start();
        }
        for (var t : threads) t.join();

        // 2) Criba de Eratóstenes: cálculo de primos con medición temporal y espacial aproximada
        System.out.println("\n=== Criba de Eratóstenes (primos ≤ " + n + ") ===");
        Measured<List<Integer>> measuredPrimes = measure(() -> sieveOfEratosthenes(n));
        System.out.println("Primos encontrados: " + measuredPrimes.result().size());
        System.out.println("Tiempo (ms): " + measuredPrimes.timeNs() / 1_000_000.0);
        System.out.println("Memoria usada (bytes, aproximado): " + measuredPrimes.memoryBytes());

        // 3) Demostración de complejidad: ordenar un array con dos algoritmos y comparar
        System.out.println("\n=== Comparación de complejidad temporal (pequeña demostración) ===");
        int m = Math.min(20_000, Math.max(1_000, n * 10)); // tamaño adaptable para la demo
        int[] arr = randomIntArray(m, 0, m);
        Measured<int[]> bubble = measure(() -> {
            int[] c = Arrays.copyOf(arr, arr.length);
            bubbleSortLimited(c, 2000); // limit iterations to avoid bloqueo en grandes m
            return c;
        });
        Measured<int[]> quick = measure(() -> {
            int[] c = Arrays.copyOf(arr, arr.length);
            Arrays.sort(c); // n log n (introsort)
            return c;
        });
        System.out.printf("BubbleSort (limit) tiempo ms: %.3f, mem bytes: %d%n", bubble.timeNs() / 1_000_000.0, bubble.memoryBytes());
        System.out.printf("Arrays.sort (n log n) tiempo ms: %.3f, mem bytes: %d%n", quick.timeNs() / 1_000_000.0, quick.memoryBytes());

        // 4) Salida breve de verificación
        System.out.println("\n=== Fin de la demo ===");
    }

    /* ---------------------------
     * Utilities y algoritmos
     * --------------------------- */

    /**
     * Convierte un entero en su representación binaria "humana" con signo (base 2).
     * Muestra cómo se vería en bits sin truncar para el tamaño de la representación Java (32 bits).
     */
    public static String toBinaryString(int value) {
        StringBuilder sb = new StringBuilder(32);
        for (int i = 31; i >= 0; i--) {
            sb.append(((value >>> i) & 1) == 1 ? '1' : '0');
            if (i % 8 == 0 && i != 0) sb.append(' '); // agrupar por bytes para legibilidad
        }
        return sb.toString();
    }

    /**
     * Calcula el complemento a dos para 'bits' bits (representación en forma de cadena).
     */
    public static String toTwosComplement(int value, int bits) {
        long mask = (1L << bits) - 1;
        long twos = value & mask;
        StringBuilder sb = new StringBuilder(bits + bits / 8);
        for (int i = bits - 1; i >= 0; i--) {
            sb.append(((twos >>> i) & 1) == 1 ? '1' : '0');
            if (i % 8 == 0 && i != 0) sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Convierte un double a su representación IEEE-754 de 64 bits como cadena de 0/1.
     */
    public static String doubleToIEEE754(double d) {
        long bits = Double.doubleToLongBits(d);
        StringBuilder sb = new StringBuilder(64 + 7);
        for (int i = 63; i >= 0; i--) {
            sb.append(((bits >>> i) & 1L) == 1L ? '1' : '0');
            if (i % 8 == 0 && i != 0) sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Criba de Eratóstenes: devuelve la lista de primos ≤ n.
     * Complejidad temporal aproximada: O(n log log n). Complejidad espacial: O(n).
     */
    public static List<Integer> sieveOfEratosthenes(int n) {
        if (n < 2) return Collections.emptyList();
        boolean[] isComposite = new boolean[n + 1];
        int limit = (int) Math.sqrt(n);
        for (int p = 2; p <= limit; p++) {
            if (!isComposite[p]) {
                for (int multiple = p * p; multiple <= n; multiple += p) {
                    isComposite[multiple] = true;
                }
            }
        }
        List<Integer> primes = new ArrayList<>();
        for (int i = 2; i <= n; i++) if (!isComposite[i]) primes.add(i);
        return primes;
    }

    /**
     * Medidor simple de tiempo (nanosegundos) y uso de memoria aproximado antes/después.
     * Retorna Measured<T> con resultado, tiempo y memoria adicional usada.
     */
    public static <T> Measured<T> measure(Callable<T> action) throws Exception {
        System.gc(); // intento de limpiar antes (no determinista)
        long beforeMem = usedMemoryBytes();
        long t0 = System.nanoTime();
        T result = action.call();
        long t1 = System.nanoTime();
        long afterMem = usedMemoryBytes();
        long memUsed = Math.max(0L, afterMem - beforeMem);
        return new Measured<>(result, t1 - t0, memUsed);
    }

    private static long usedMemoryBytes() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    /**
     * Genera un arreglo de enteros aleatorios (para demostración de ordenamiento).
     */
    public static int[] randomIntArray(int size, int minInclusive, int maxExclusive) {
        Random r = new Random(12345);
        return IntStream.range(0, size).map(i -> r.nextInt(maxExclusive - minInclusive) + minInclusive).toArray();
    }

    /**
     * Bubble sort limitado en iteraciones. Usado solo para demostrar la diferencia de complejidad.
     * Si el array es grande, se evita que la operación dure indefinidamente.
     * Complejidad típica: O(n^2) (peor caso).
     */
    public static void bubbleSortLimited(int[] a, int maxSwaps) {
        int n = a.length;
        int swaps = 0;
        boolean swapped;
        for (int i = 0; i < n - 1 && swaps < maxSwaps; i++) {
            swapped = false;
            for (int j = 0; j < n - 1 - i && swaps < maxSwaps; j++) {
                if (a[j] > a[j + 1]) {
                    int tmp = a[j]; a[j] = a[j + 1]; a[j + 1] = tmp;
                    swapped = true;
                    swaps++;
                }
            }
            if (!swapped) break;
        }
    }
}
