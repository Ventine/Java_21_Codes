/**
 * ConceptsDemo.java
 *
 * Demostración práctica de:
 * - Notación Big-O: comparación de algoritmos con diferentes órdenes de crecimiento.
 * - Arquitectura de computadores: efectos de caché y acceso secuencial vs aleatorio.
 * - Concurrencia y paralelismo: uso de virtual threads para concurrencia y parallel streams
 *   para paralelismo real sobre múltiples núcleos.
 *
 * Compilación:
 *   javac --enable-preview --release 21 ConceptsDemo.java
 *
 * Ejecución:
 *   java --enable-preview ConceptsDemo
 */
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public final class ConceptsDemo {

    public static void main(String[] args) throws Exception {

        System.out.println("=== Big-O: comparación directa ===");
        compareBigO();

        System.out.println("\n=== Arquitectura de computadores: caché y acceso a memoria ===");
        demonstrateMemoryAccess();

        System.out.println("\n=== Concurrencia (virtual threads) y paralelismo (parallel streams) ===");
        demonstrateConcurrencyAndParallelism();
    }

    /* ============================================================
       SECCIÓN 1: NOTACIÓN BIG-O
       ============================================================ */

    /**
     * Compara tiempos de tres algoritmos representativos de distintas
     * complejidades: O(1), O(n) y O(n^2). Uso didáctico, no benchmark.
     */
    private static void compareBigO() {
        int n = 20_000;

        long t1 = measure(() -> constantTimeOperation());
        long t2 = measure(() -> linearOperation(n));
        long t3 = measure(() -> quadraticOperation(2000)); // acotado para evitar tiempos excesivos

        System.out.println("O(1):  " + t1 + " ns");
        System.out.println("O(n):  " + t2 + " ns (n=" + n + ")");
        System.out.println("O(n^2): " + t3 + " ns (n=2000)");
    }

    /** O(1): operación fija. */
    private static int constantTimeOperation() {
        return 42 + 7;
    }

    /** O(n): recorre la entrada una vez. */
    private static long linearOperation(int n) {
        long sum = 0;
        for (int i = 0; i < n; i++) sum += i;
        return sum;
    }

    /** O(n^2): dos bucles anidados. */
    private static long quadraticOperation(int n) {
        long count = 0;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                count += j;
        return count;
    }

    /* ============================================================
       SECCIÓN 2: ARQUITECTURA DE COMPUTADORES
       ACCESO A MEMORIA Y EFECTOS DE CACHÉ
       ============================================================ */

    /**
     * Demuestra la diferencia entre acceso secuencial (aprovecha caché)
     * y acceso aleatorio (más fallos de caché).
     */
    private static void demonstrateMemoryAccess() {
        int size = 10_000_000;
        int[] array = new int[size];
        Arrays.fill(array, 1);

        long sequential = measure(() -> {
            long sum = 0;
            for (int i = 0; i < array.length; i++) sum += array[i];
            return sum;
        });

        long random = measure(() -> {
            long sum = 0;
            Random r = new Random(123);
            for (int i = 0; i < array.length; i++)
                sum += array[r.nextInt(array.length)];
            return sum;
        });

        System.out.println("Acceso secuencial: " + sequential / 1_000_000.0 + " ms");
        System.out.println("Acceso aleatorio:   " + random / 1_000_000.0 + " ms");
    }

    /* ============================================================
       SECCIÓN 3: CONCURRENCIA Y PARALELISMO
       ============================================================ */

    /**
     * Ejecuta dos demostraciones:
     * - Concurrencia: muchas tareas I/O-simuladas usando virtual threads.
     * - Paralelismo: operación computacional usando múltiples núcleos.
     */
    private static void demonstrateConcurrencyAndParallelism() throws Exception {

        System.out.println("Concurrencia (virtual threads)...");
        int tasks = 1000;
        long t1 = measure(() -> runConcurrentTasks(tasks));

        System.out.println("Tiempo virtual threads (tasks=" + tasks + "): " + t1 / 1_000_000.0 + " ms");

        System.out.println("Paralelismo (parallel stream)...");
        int size = 5_000_000;
        long t2 = measure(() -> parallelComputation(size));

        System.out.println("Tiempo parallel stream: " + t2 / 1_000_000.0 + " ms");
    }

    /**
     * Crea muchas tareas que simulan I/O (sleep) ejecutadas en virtual threads.
     * Esto demuestra concurrencia con bajo costo por hilo.
     */
    private static void runConcurrentTasks(int count) {
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                tasks.add(() -> {
                    Thread.sleep(1); // simula I/O
                    return null;
                });
            }
            exec.invokeAll(tasks);
        } catch (InterruptedException ignored) {}
    }

    /**
     * Realiza una operación intensiva en CPU usando múltiples núcleos reales.
     * Demuestra paralelismo.
     */
    private static long parallelComputation(int size) {
        return IntStream.range(0, size)
                .parallel()       // paralelismo real por núcleos
                .mapToLong(i -> i % 3 == 0 ? i * 2 : i * 3)
                .sum();
    }

    /* ============================================================
       UTILIDADES
       ============================================================ */

    /**
     * Mide el tiempo de ejecución en nanosegundos de una acción.
     */
    private static long measure(Runnable action) {
        long t0 = System.nanoTime();
        action.run();
        long t1 = System.nanoTime();
        return t1 - t0;
    }

    private static long measure(Callable<?> action) {
        long t0 = System.nanoTime();
        try { action.call(); } catch (Exception ignored) {}
        long t1 = System.nanoTime();
        return t1 - t0;
    }
}
