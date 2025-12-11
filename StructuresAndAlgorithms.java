/**
 * Estructuras y algoritmos:
 * 1. Segment Tree
 * 2. Fenwick Tree (Binary Indexed Tree)
 * 3. Quicksort
 */
public class StructuresAndAlgorithms {

    /* =========================================================
       SEGMENT TREE
       ========================================================= */

    /**
     * SegmentTree para rangos de suma.
     * Cada nodo almacena la suma del intervalo.
     */
    public static class SegmentTree {
        private final int n;
        private final int[] tree;

        /**
         * Construcción en O(n).
         */
        public SegmentTree(int[] arr) {
            this.n = arr.length;
            this.tree = new int[4 * n];
            build(arr, 1, 0, n - 1);
        }

        private void build(int[] arr, int node, int l, int r) {
            if (l == r) {
                tree[node] = arr[l];
                return;
            }
            int mid = (l + r) >>> 1;
            build(arr, node << 1, l, mid);
            build(arr, (node << 1) + 1, mid + 1, r);
            tree[node] = tree[node << 1] + tree[(node << 1) + 1];
        }

        /**
         * Consulta de suma en rango [ql, qr].
         */
        public int query(int ql, int qr) {
            return query(1, 0, n - 1, ql, qr);
        }

        private int query(int node, int l, int r, int ql, int qr) {
            if (qr < l || ql > r) return 0;
            if (ql <= l && r <= qr) return tree[node];
            int mid = (l + r) >>> 1;
            int left = query(node << 1, l, mid, ql, qr);
            int right = query((node << 1) + 1, mid + 1, r, ql, qr);
            return left + right;
        }

        /**
         * Actualización puntual: arr[idx] += value.
         */
        public void update(int idx, int value) {
            update(1, 0, n - 1, idx, value);
        }

        private void update(int node, int l, int r, int idx, int value) {
            if (l == r) {
                tree[node] += value;
                return;
            }
            int mid = (l + r) >>> 1;
            if (idx <= mid) update(node << 1, l, mid, idx, value);
            else update((node << 1) + 1, mid + 1, r, idx, value);
            tree[node] = tree[node << 1] + tree[(node << 1) + 1];
        }
    }

    /* =========================================================
       FENWICK TREE
       ========================================================= */

    /**
     * Fenwick Tree (BIT) para sumas prefix.
     */
    public static class FenwickTree {
        private final int n;
        private final int[] fenwick;

        public FenwickTree(int n) {
            this.n = n;
            this.fenwick = new int[n + 1];
        }

        /**
         * Aumenta arr[i] += value.
         */
        public void update(int i, int value) {
            for (; i <= n; i += i & -i) fenwick[i] += value;
        }

        /**
         * Suma prefix [1..i].
         */
        public int query(int i) {
            int sum = 0;
            for (; i > 0; i -= i & -i) sum += fenwick[i];
            return sum;
        }

        /**
         * Suma en rango [l, r].
         */
        public int rangeQuery(int l, int r) {
            return query(r) - query(l - 1);
        }
    }

    /* =========================================================
       QUICKSORT
       ========================================================= */

    /**
     * Implementación de Quicksort in-place.
     */
    public static void quicksort(int[] arr) {
        quicksort(arr, 0, arr.length - 1);
    }

    private static void quicksort(int[] arr, int low, int high) {
        if (low >= high) return;
        int p = partition(arr, low, high);
        quicksort(arr, low, p - 1);
        quicksort(arr, p + 1, high);
    }

    /**
     * Partición estilo Lomuto.
     */
    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low;
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                swap(arr, i, j);
                i++;
            }
        }
        swap(arr, i, high);
        return i;
    }

    private static void swap(int[] arr, int a, int b) {
        int t = arr[a];
        arr[a] = arr[b];
        arr[b] = t;
    }

    /* =========================================================
       DEMO
       ========================================================= */
    public static void main(String[] args) {
        int[] base = {1, 3, 5, 7, 9, 11};

        SegmentTree st = new SegmentTree(base);
        int r1 = st.query(1, 4);
        st.update(3, 5);

        FenwickTree ft = new FenwickTree(6);
        for (int i = 0; i < base.length; i++) ft.update(i + 1, base[i]);
        int r2 = ft.rangeQuery(2, 5);

        int[] q = {10, 7, 2, 15, 3};
        quicksort(q);
    }
}
