/*
 * BinaryTreesDemo.java
 *
 * Ejemplo compacto en Java 21 con:
 *  - Árbol binario de búsqueda (BST)
 *  - Árbol AVL
 *  - Árbol Red-Black
 *
 * Propósito: mostrar estructuras y operaciones básicas.
 */

public final class BinaryTreesDemo {
    public static void main(String[] args) {
        System.out.println("=== BST ===");
        var bst = new BST();
        bst.insert(8);
        bst.insert(3);
        bst.insert(10);
        bst.insert(1);
        bst.insert(6);
        bst.inOrder();

        System.out.println("\n=== AVL ===");
        var avl = new AVL();
        avl.insert(20);
        avl.insert(4);
        avl.insert(26);
        avl.insert(3);
        avl.insert(9);
        avl.insert(15); // fuerza rotaciones
        avl.inOrder();

        System.out.println("\n=== RedBlackTree ===");
        var rbt = new RedBlackTree();
        rbt.insert(7);
        rbt.insert(3);
        rbt.insert(18);
        rbt.insert(10);
        rbt.insert(22);
        rbt.insert(8);
        rbt.insert(11);
        rbt.insert(26);
        rbt.inOrder();
    }
}

/* ============================================================
   BST SIMPLE
   ============================================================ */

final class BST {
    private static final class Node {
        int key;
        Node left, right;
        Node(int k) { key = k; }
    }

    private Node root;

    public void insert(int k) {
        root = insertRec(root, k);
    }

    private Node insertRec(Node n, int k) {
        if (n == null) return new Node(k);
        if (k < n.key) n.left = insertRec(n.left, k);
        else n.right = insertRec(n.right, k);
        return n;
    }

    public void inOrder() { inOrderRec(root); System.out.println(); }

    private void inOrderRec(Node n) {
        if (n == null) return;
        inOrderRec(n.left);
        System.out.print(n.key + " ");
        inOrderRec(n.right);
    }
}

/* ============================================================
   AVL
   ============================================================ */

final class AVL {
    private static final class Node {
        int key, height;
        Node left, right;
        Node(int k) { key = k; height = 1; }
    }

    private Node root;

    public void insert(int k) { root = insertRec(root, k); }

    private Node insertRec(Node n, int k) {
        if (n == null) return new Node(k);

        if (k < n.key) n.left = insertRec(n.left, k);
        else n.right = insertRec(n.right, k);

        updateHeight(n);
        return balance(n);
    }

    private void updateHeight(Node n) {
        n.height = 1 + Math.max(h(n.left), h(n.right));
    }

    private int h(Node n) { return n == null ? 0 : n.height; }

    private int balanceFactor(Node n) { return h(n.left) - h(n.right); }

    private Node balance(Node n) {
        int bf = balanceFactor(n);

        if (bf > 1 && balanceFactor(n.left) >= 0) return rotateRight(n);
        if (bf > 1 && balanceFactor(n.left) < 0) {
            n.left = rotateLeft(n.left);
            return rotateRight(n);
        }
        if (bf < -1 && balanceFactor(n.right) <= 0) return rotateLeft(n);
        if (bf < -1 && balanceFactor(n.right) > 0) {
            n.right = rotateRight(n.right);
            return rotateLeft(n);
        }

        return n;
    }

    private Node rotateRight(Node y) {
        Node x = y.left;
        Node t = x.right;
        x.right = y;
        y.left = t;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    private Node rotateLeft(Node x) {
        Node y = x.right;
        Node t = y.left;
        y.left = x;
        x.right = t;
        updateHeight(x);
        updateHeight(y);
        return y;
    }

    public void inOrder() { inOrderRec(root); System.out.println(); }

    private void inOrderRec(Node n) {
        if (n == null) return;
        inOrderRec(n.left);
        System.out.print(n.key + " ");
        inOrderRec(n.right);
    }
}

/* ============================================================
   RED-BLACK TREE (insersión + recoloreo + rotaciones)
   ============================================================ */

final class RedBlackTree {

    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private static final class Node {
        int key;
        Node left, right, parent;
        boolean color = RED;
        Node(int k) { key = k; }
    }

    private Node root;

    public void insert(int key) {
        Node n = new Node(key);
        root = bstInsert(root, n);
        fixInsert(n);
    }

    private Node bstInsert(Node root, Node n) {
        if (root == null) return n;

        Node cur = root;
        Node parent = null;
        while (cur != null) {
            parent = cur;
            if (n.key < cur.key) cur = cur.left;
            else cur = cur.right;
        }
        n.parent = parent;
        if (n.key < parent.key) parent.left = n;
        else parent.right = n;

        return root;
    }

    private void fixInsert(Node n) {
        while (n != root && n.parent.color == RED) {
            Node p = n.parent;
            Node g = p.parent;

            if (p == g.left) {
                Node u = g.right;
                if (u != null && u.color == RED) {
                    g.color = RED;
                    p.color = BLACK;
                    u.color = BLACK;
                    n = g;
                } else {
                    if (n == p.right) {
                        n = p;
                        rotateLeft(n);
                    }
                    p.color = BLACK;
                    g.color = RED;
                    rotateRight(g);
                }
            } else {
                Node u = g.left;
                if (u != null && u.color == RED) {
                    g.color = RED;
                    p.color = BLACK;
                    u.color = BLACK;
                    n = g;
                } else {
                    if (n == p.left) {
                        n = p;
                        rotateRight(n);
                    }
                    p.color = BLACK;
                    g.color = RED;
                    rotateLeft(g);
                }
            }
        }
        root.color = BLACK;
    }

    private void rotateLeft(Node x) {
        Node y = x.right;
        x.right = y.left;
        if (y.left != null) y.left.parent = x;
        y.parent = x.parent;

        if (x.parent == null) root = y;
        else if (x == x.parent.left) x.parent.left = y;
        else x.parent.right = y;

        y.left = x;
        x.parent = y;
    }

    private void rotateRight(Node y) {
        Node x = y.left;
        y.left = x.right;
        if (x.right != null) x.right.parent = y;
        x.parent = y.parent;

        if (y.parent == null) root = x;
        else if (y == y.parent.left) y.parent.left = x;
        else y.parent.right = x;

        x.right = y;
        y.parent = x;
    }

    public void inOrder() { inOrderRec(root); System.out.println(); }

    private void inOrderRec(Node n) {
        if (n == null) return;
        inOrderRec(n.left);
        System.out.print(n.key + " ");
        inOrderRec(n.right);
    }
}
