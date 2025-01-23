package net.minestom.server.instance.chunksystem.impl;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class IntervalTree<T> {

    static final boolean RED = false;
    static final boolean BLACK = true;

    private Node<T> root;
    private int size = 0;
    private int modCount;

    // Constructor to initialize the Red-Black Tree
    public IntervalTree() {
    }

    private void addEntry(int start, int end, T data, Node<T> parent, boolean addToLeft) {
        Node<T> e = new Node<>(start, end, data, parent);
        if (addToLeft) parent.left = e;
        else parent.right = e;
        fixAfterInsertion(e);
        size++;
        modCount++;
    }

    private void addEntryToEmptyMap(int start, int end, T data) {
        root = new Node<>(start, end, data, null);
        size = 1;
        modCount++;
    }

    public void clear() {
        root = null;
        size = 0;
        modCount++;
    }

    public boolean insert(int start, int end, T data) {
        return put(start, end, data);
    }

    public T insertOrGet(int start, int end, Supplier<T> supplier) {
        return put(start, end, supplier);
    }

    public boolean delete(int start) {
        var node = getEntry(start);
        if (node == null) return false;
        deleteEntry(node);
        return true;
    }

    public List<Node<T>> searchNodes(int point) {
        var nodes = new ArrayList<Node<T>>();
        search(root, point, nodes);
        return nodes;
    }

    private void search(Node<T> node, int point, List<Node<T>> result) {
        if (node == null) return;
        if (point > node.maxEnd) return;


        // left checks handled by recursion
        search(node.left, point, result);

        if (point >= node.start && point <= node.end) {
            result.add(node);
        }

        if (point >= node.start) {
            search(node.right, point, result);
        }
    }

    public Node<T> getRoot() {
        return root;
    }

    private boolean put(int start, int end, T data) {
        Node<T> t = root;
        if (t == null) {
            addEntryToEmptyMap(start, end, data);
            return true;
        }
        int cmp;
        Node<T> parent;
        do {
            parent = t;

            // update maxEnd for all parents on the way down
            t.maxEnd = Math.max(t.maxEnd, end);

            cmp = Integer.compare(start, t.start);
            if (cmp == 0) {
                // already contains
                return false;
            }
            t = cmp < 0 ? t.left : t.right;
        } while (t != null);
        addEntry(start, end, data, parent, cmp < 0);
        return true;
    }

    private T put(int start, int end, Supplier<T> supplier) {
        Node<T> t = root;
        if (t == null) {
            T data = supplier.get();
            addEntryToEmptyMap(start, end, data);
            return data;
        }
        int cmp;
        Node<T> parent;
        do {
            parent = t;

            // update maxEnd for all parents on the way down
            t.maxEnd = Math.max(t.maxEnd, end);

            cmp = Integer.compare(start, t.start);
            if (cmp == 0) {
                // already contains
                return t.data;
            }
            t = cmp < 0 ? t.left : t.right;
        } while (t != null);
        T data = supplier.get();
        addEntry(start, end, data, parent, cmp < 0);
        return data;
    }

    /**
     * Returns this map's entry for the given key, or {@code null} if the map
     * does not contain an entry for the key.
     *
     * @return this map's entry for the given key, or {@code null} if the map
     * does not contain an entry for the key
     * @throws ClassCastException   if the specified key cannot be compared
     *                              with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *                              and this map uses natural ordering, or its comparator
     *                              does not permit null keys
     */
    final Node<T> getEntry(int key) {
        // Offload comparator-based version for sake of performance
        Node<T> p = root;
        while (p != null) {
            int cmp = Integer.compare(key, p.start);
            if (cmp < 0) p = p.left;
            else if (cmp > 0) p = p.right;
            else return p;
        }
        return null;
    }

    /**
     * From CLR
     */
    private void rotateLeft(Node<T> p) {
        if (p != null) {
            Node<T> r = p.right;
            p.right = r.left;
            if (r.left != null) r.left.parent = p;
            r.parent = p.parent;
            if (p.parent == null) root = r;
            else if (p.parent.left == p) p.parent.left = r;
            else p.parent.right = r;
            r.left = p;
            p.parent = r;

            p.resetMaxEnd();
            r.resetMaxEnd();
        }
    }

    /**
     * From CLR
     */
    private void rotateRight(Node<T> p) {
        if (p != null) {
            Node<T> l = p.left;
            p.left = l.right;
            if (l.right != null) l.right.parent = p;
            l.parent = p.parent;
            if (p.parent == null) root = l;
            else if (p.parent.right == p) p.parent.right = l;
            else p.parent.left = l;
            l.right = p;
            p.parent = l;

            p.resetMaxEnd();
            l.resetMaxEnd();
        }
    }

    private void fixMaxEnd(Node<T> n) {
        do {
            n.resetMaxEnd();
            n = n.parent;
        } while (n != null);
    }

    /**
     * From CLR
     */
    private void fixAfterInsertion(Node<T> x) {
        x.color = RED;

        while (x != null && x != root && x.parent.color == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                Node<T> y = rightOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateRight(parentOf(parentOf(x)));
                }
            } else {
                Node<T> y = leftOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        root.color = BLACK;
    }

    /**
     * Delete node p, and then rebalance the tree.
     */
    private void deleteEntry(Node<T> p) {
        modCount++;
        size--;

        // If strictly internal, copy successor's element to p and then make p
        // point to successor.
        if (p.left != null && p.right != null) {
            Node<T> s = successor(p);
            p.replaceData(s);
            fixMaxEnd(p);
            p = s;
        } // p has 2 children

        // Start fixup at replacement node, if it exists.
        Node<T> replacement = (p.left != null ? p.left : p.right);

        if (replacement != null) {
            // Link replacement to parent
            replacement.parent = p.parent;
            if (p.parent == null) root = replacement;
            else if (p == p.parent.left) {
                p.parent.left = replacement;
                fixMaxEnd(p);
            } else {
                p.parent.right = replacement;
                fixMaxEnd(p);
            }

            // Null out links so they are OK to use by fixAfterDeletion.
            p.left = p.right = p.parent = null;

            // Fix replacement
            if (p.color == BLACK) fixAfterDeletion(replacement);
        } else if (p.parent == null) { // return if we are the only node.
            root = null;
        } else { //  No children. Use self as phantom replacement and unlink.
            if (p.color == BLACK) fixAfterDeletion(p);

            if (p.parent != null) {
                if (p == p.parent.left) p.parent.left = null;
                else if (p == p.parent.right) p.parent.right = null;
                p.parent = null;
            }
        }
    }

    /**
     * From CLR
     */
    private void fixAfterDeletion(Node<T> x) {
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                Node<T> sib = rightOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }

                if (colorOf(leftOf(sib)) == BLACK && colorOf(rightOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sib), BLACK);
                    rotateLeft(parentOf(x));
                    x = root;
                }
            } else { // symmetric
                Node<T> sib = leftOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }

                if (colorOf(rightOf(sib)) == BLACK && colorOf(leftOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib), BLACK);
                    rotateRight(parentOf(x));
                    x = root;
                }
            }
        }

        setColor(x, BLACK);
    }

    private static boolean colorOf(Node<?> p) {
        return (p == null ? BLACK : p.color);
    }

    private static <T> Node<T> parentOf(Node<T> p) {
        return (p == null ? null : p.parent);
    }

    private static void setColor(Node<?> p, boolean c) {
        if (p != null) p.color = c;
    }

    private static <T> Node<T> leftOf(Node<T> p) {
        return (p == null) ? null : p.left;
    }

    private static <T> Node<T> rightOf(Node<T> p) {
        return (p == null) ? null : p.right;
    }

    /**
     * Returns the successor of the specified Entry, or null if no such.
     */
    static <T> Node<T> successor(Node<T> t) {
        if (t == null) return null;
        else if (t.right != null) {
            Node<T> p = t.right;
            while (p.left != null) p = p.left;
            return p;
        } else {
            Node<T> p = t.parent;
            Node<T> ch = t;
            while (p != null && ch == p.right) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    /**
     * Returns the predecessor of the specified Entry, or null if no such.
     */
    static <T> Node<T> predecessor(Node<T> t) {
        if (t == null) return null;
        else if (t.left != null) {
            Node<T> p = t.left;
            while (p.right != null) p = p.right;
            return p;
        } else {
            Node<T> p = t.parent;
            Node<T> ch = t;
            while (p != null && ch == p.left) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    public int size() {
        return size;
    }

    public boolean equals(Object other) {
        if (!(other instanceof IntervalTree<?> tree)) return false;
        if (size != tree.size) return false;
        return equals(root, tree.root);
    }

    private boolean equals(Node<?> node, Node<?> o) {
        if (node == null && o == null) return true;
        if (node == null || o == null) return false;
        return node.equalsDown(o);
    }

    public int height() {
        return height(root);
    }

    public IntervalTree<T> copy() {
        var tree = new IntervalTree<T>();
        tree.root = deepCopy(root);
        tree.size = size;
        return tree;
    }

    private Node<T> deepCopy(Node<T> origin) {
        if (origin == null) return null;
        var node = copySingleWithoutLinks(origin);
        var left = deepCopy(origin.left);
        var right = deepCopy(origin.right);
        if (left != null) {
            left.parent = node;
            node.left = left;
        }
        if (right != null) {
            right.parent = node;
            node.right = right;
        }
        return node;
    }

    private Node<T> copySingleWithoutLinks(Node<T> origin) {
        if (origin == null) return null;
        var node = new Node<T>(0, 0, null, null);
        node.replaceData(origin);
        node.maxEnd = origin.maxEnd;
        node.color = origin.color;
        return node;
    }

    public int[] preOrder() {
        var list = new ArrayList<Integer>();
        preOrder(n -> list.add(n.start), root);
        return list.stream().mapToInt(i -> i).toArray();
    }

    public void preOrder(Consumer<Node<T>> consumer) {
        preOrder(consumer, root);
    }

    public int[] inOrder() {
        var list = new ArrayList<Integer>();
        inOrder(list, root);
        return list.stream().mapToInt(i -> i).toArray();
    }

    private void preOrder(Consumer<Node<T>> consumer, Node<T> node) {
        if (node == null) return;
        consumer.accept(node);
        preOrder(consumer, node.left);
        preOrder(consumer, node.right);
    }

    private void inOrder(ArrayList<Integer> list, Node<T> node) {
        if (node == null) return;
        inOrder(list, node.left);
        list.add(node.start);
        inOrder(list, node.right);
    }

    private int height(Node<T> node) {
        if (node == null) return 0;
        return 1 + Math.max(height(node.left), height(node.right));
    }

    public static class Node<T> {
        public T data;
        public int start;
        public int end;
        public Node<T> left, right, parent;
        public boolean color;
        public int maxEnd;

        public Node(int start, int end, T data, @Nullable IntervalTree.Node<T> parent) {
            this.start = start;
            this.end = end;
            this.data = data;
            this.maxEnd = end;
            this.parent = parent;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Node<?> other)) return false;
            return equalsDown(other) && equalsUp(other);
        }

        private boolean equalsUp(Node<?> other) {
            if (other == null) return false;
            if (hasDifferingValues(other)) return false;
            if (parent == null && other.parent == null) return true;
            if (parent == null || other.parent == null) return false;
            return parent.equalsUp(other.parent);
        }

        private boolean equalsDown(Node<?> other) {
            if (other == null) return false;
            if (hasDifferingValues(other)) return false;
            if (left != null) {
                if (!left.equalsDown(other.left)) return false;
            } else if (other.left != null) return false;
            if (right != null) {
                return right.equalsDown(other.right);
            } else return other.right == null;
        }

        private boolean hasDifferingValues(Node<?> other) {
            return start != other.start || end != other.end || maxEnd != other.maxEnd || color != other.color || !Objects.equals(data, other.data);
        }

        private void resetMaxEnd() {
            maxEnd = calculateMaxEnd();
        }

        public int calculateMaxEnd() {
            var val = end;
            if (left != null) {
                val = Math.max(val, left.maxEnd);
            }
            if (right != null) {
                val = Math.max(val, right.maxEnd);
            }
            return val;
        }

        private void replaceData(Node<T> other) {
            this.start = other.start;
            this.end = other.end;
            this.data = other.data;
        }

        @Override
        public String toString() {
            return "[%d,%d]".formatted(start, end);
        }
    }
}
