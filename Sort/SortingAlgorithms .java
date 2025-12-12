/**
 * Implementaciones de:
 * 1. Mergesort
 * 2. Heapsort
 * 3. Radix Sort (enteros no negativos)
 */
public class SortingAlgorithms {

    /* =========================================================
       MERGESORT
       ========================================================= */

    /**
     * Ordenamiento mergesort: divide el arreglo, ordena mitades,
     * fusiona en orden ascendente.
     */
    public static void mergesort(int[] arr) {
        if (arr.length < 2) return;
        int[] aux = new int[arr.length];
        mergesort(arr, aux, 0, arr.length - 1);
    }

    private static void mergesort(int[] arr, int[] aux, int left, int right) {
        if (left >= right) return;
        int mid = (left + right) >>> 1;
        mergesort(arr, aux, left, mid);
        mergesort(arr, aux, mid + 1, right);
        merge(arr, aux, left, mid, right);
    }

    private static void merge(int[] arr, int[] aux, int left, int mid, int right) {
        int i = left;
        int j = mid + 1;
        int k = left;

        while (i <= mid && j <= right) {
            aux[k++] = (arr[i] <= arr[j]) ? arr[i++] : arr[j++];
        }
        while (i <= mid) aux[k++] = arr[i++];
        while (j <= right) aux[k++] = arr[j++];

        for (int p = left; p <= right; p++) arr[p] = aux[p];
    }

    /* =========================================================
       HEAPSORT
       ========================================================= */

    /**
     * Heapsort con max-heap. Reordena el arreglo in-place.
     */
    public static void heapsort(int[] arr) {
        int n = arr.length;

        // Construcción del max-heap
        for (int i = (n / 2) - 1; i >= 0; i--) heapify(arr, n, i);

        // Extracción del máximo y reducción del heap
        for (int end = n - 1; end > 0; end--) {
            swap(arr, 0, end);
            heapify(arr, end, 0);
        }
    }

    private static void heapify(int[] arr, int size, int root) {
        int largest = root;
        int left = (root << 1) + 1;
        int right = (root << 1) + 2;

        if (left < size && arr[left] > arr[largest]) largest = left;
        if (right < size && arr[right] > arr[largest]) largest = right;

        if (largest != root) {
            swap(arr, root, largest);
            heapify(arr, size, largest);
        }
    }

    private static void swap(int[] arr, int a, int b) {
        int t = arr[a];
        arr[a] = arr[b];
        arr[b] = t;
    }

    /* =========================================================
       RADIX SORT (base 10)
       ========================================================= */

    /**
     * Radix sort para enteros no negativos.
     * Procesa cada dígito con counting sort estable.
     */
    public static void radixSort(int[] arr) {
        int max = max(arr);

        for (int exp = 1; max / exp > 0; exp *= 10) {
            countingSortByDigit(arr, exp);
        }
    }

    private static void countingSortByDigit(int[] arr, int exp) {
        int n = arr.length;
        int[] output = new int[n];
        int[] count = new int[10]; // dígitos 0-9

        // Conteo de dígitos
        for (int value : arr) {
            int digit = (value / exp) % 10;
            count[digit]++;
        }

        // Prefijos acumulados
        for (int i = 1; i < 10; i++) count[i] += count[i - 1];

        // Construcción estable del arreglo ordenado
        for (int i = n - 1; i >= 0; i--) {
            int digit = (arr[i] / exp) % 10;
            output[--count[digit]] = arr[i];
        }

        // Copia al arreglo original
        System.arraycopy(output, 0, arr, 0, n);
    }

    private static int max(int[] arr) {
        int m = arr[0];
        for (int v : arr) if (v > m) m = v;
        return m;
    }

    /* =========================================================
       DEMO
       ========================================================= */
    public static void main(String[] args) {
        int[] a = {8, 3, 1, 7, 4, 2, 6};
        mergesort(a);

        int[] b = {9, 5, 2, 8, 1, 4};
        heapsort(b);

        int[] c = {170, 45, 75, 90, 802, 24, 2, 66};
        radixSort(c);
    }
}
