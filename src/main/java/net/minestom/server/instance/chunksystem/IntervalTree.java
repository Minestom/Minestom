package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Modified RBTree taken from {@link TreeSet} to support intervals.
 * Every node contains an extra maxEnd and a {@link TreeMap}, more information
 * can be taken on from
 * <a href="https://en.wikipedia.org/w/index.php?title=Interval_tree&oldid=1233051410">Wikipedia</a>
 *
 * @param <T>
 */
@SuppressWarnings("unused")
public class IntervalTree<T> {

    static final boolean RED = false;
    static final boolean BLACK = true;

    private @Nullable Node<T> root;
    private int size = 0;
    private int modCount;

    private void addEntry(int start, int end, T data, Node<T> parent, boolean addToLeft) {
        Node<T> e = new Node<>(start, end, data, parent);
        if (addToLeft) parent.left = e;
        else parent.right = e;
        this.fixAfterInsertion(e);
        this.size++;
        this.modCount++;
    }

    public int state() {
        return modCount;
    }

    public boolean modifiedSince(int state) {
        return modCount != state;
    }

    private void addEntryToEmptyMap(int start, int end, T data) {
        this.root = new Node<>(start, end, data, null);
        this.size = 1;
        this.modCount++;
    }

    public void clear() {
        this.root = null;
        this.size = 0;
        this.modCount++;
    }

    @Override
    public String toString() {
        return Node.toStringRecursive(this.root);
    }

    public T insertOrGet(int start, int end, Supplier<T> supplier) {
        return this.put(start, end, supplier);
    }

    @Nullable
    public T get(int start, int end) {
        var entry = getEntry(start);
        if (entry == null) return null;
        return entry.end.get(end);
    }

    public boolean delete(int start, int end) {
        var node = this.getEntry(start);
        if (node == null) return false;
        if (node.delete(end)) {
            if (node.end.isEmpty()) {
                this.deleteEntry(node);
            }
            return true;
        }
        return false;
    }

    public List<Node<T>> searchNodes(int point) {
        var nodes = this.search(this.root, point, null);
        return nodes == null ? List.of() : nodes.collect();
    }

    public void searchNodes(ReusableList<Node<T>> targetList, int point) {
        this.search(this.root, point, targetList);
    }

    private @Nullable ReusableList<Node<T>> search(@Nullable Node<T> node, int point, @Nullable ReusableList<Node<T>> result) {
        if (node == null) return result;
        if (point > node.maxEnd) return result;


        // left checks handled by recursion
        result = this.search(node.left, point, result);

        if (point >= node.start && point <= node.end.lastIntKey()) {
            if (result == null) result = new ReusableList<>();
            result.add(node);
        }

        if (point >= node.start) {
            result = this.search(node.right, point, result);
        }
        return result;
    }

    public @Nullable Node<T> getRoot() {
        return this.root;
    }

    private T put(int start, int end, Supplier<T> supplier) {
        Node<T> t = this.root;
        if (t == null) {
            T data = supplier.get();
            this.addEntryToEmptyMap(start, end, data);
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
                if (t.end.containsKey(end)) {
                    return t.end.get(end);
                }
                modCount++;
                return t.forceAdd(end, supplier.get());
            }
            t = cmp < 0 ? t.left : t.right;
        } while (t != null);
        T data = supplier.get();
        this.addEntry(start, end, data, parent, cmp < 0);
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
    final @Nullable Node<T> getEntry(int key) {
        // Offload comparator-based version for sake of performance
        Node<T> p = this.root;
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
    private void rotateLeft(@Nullable Node<T> p) {
        if (p != null) {
            assert p.right != null;
            Node<T> r = p.right;
            p.right = r.left;
            if (r.left != null) r.left.parent = p;
            r.parent = p.parent;
            if (p.parent == null) this.root = r;
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
    private void rotateRight(@Nullable Node<T> p) {
        if (p != null) {
            assert p.left != null;
            Node<T> l = p.left;
            p.left = l.right;
            if (l.right != null) l.right.parent = p;
            l.parent = p.parent;
            if (p.parent == null) this.root = l;
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
        assert this.root != null;
        x.color = RED;

        while (x != null && x != this.root) {
            assert x.parent != null;
            if (!(x.parent.color == RED)) break;

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
        this.root.color = BLACK;
    }

    /**
     * Delete node p, and then rebalance the tree.
     */
    private void deleteEntry(Node<T> p) {
        this.modCount++;
        this.size--;

        // If strictly internal, copy successor's element to p and then make p
        // point to successor.
        if (p.left != null && p.right != null) {
            Node<T> s = successor(p);
            assert s != null;
            p.replaceData(s);
            this.fixMaxEnd(p);
            p = s;
        } // p has 2 children

        // Start fixup at replacement node, if it exists.
        Node<T> replacement = (p.left != null ? p.left : p.right);

        if (replacement != null) {
            // Link replacement to parent
            replacement.parent = p.parent;
            if (p.parent == null) this.root = replacement;
            else if (p == p.parent.left) {
                p.parent.left = replacement;
                this.fixMaxEnd(p.parent);
            } else {
                p.parent.right = replacement;
                this.fixMaxEnd(p.parent);
            }

            // Null out links so they are OK to use by fixAfterDeletion.
            p.left = p.right = p.parent = null;

            // Fix replacement
            if (p.color == BLACK) this.fixAfterDeletion(replacement);
        } else if (p.parent == null) { // return if we are the only node.
            this.root = null;
        } else { //  No children. Use self as phantom replacement and unlink.
            if (p.color == BLACK) this.fixAfterDeletion(p);

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
        while (x != this.root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                Node<T> sib = rightOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    this.rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }

                if (colorOf(leftOf(sib)) == BLACK && colorOf(rightOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK);
                        setColor(sib, RED);
                        this.rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sib), BLACK);
                    this.rotateLeft(parentOf(x));
                    x = this.root;
                }
            } else { // symmetric
                Node<T> sib = leftOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    this.rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }

                if (colorOf(rightOf(sib)) == BLACK && colorOf(leftOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK);
                        setColor(sib, RED);
                        this.rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib), BLACK);
                    this.rotateRight(parentOf(x));
                    x = this.root;
                }
            }
        }

        setColor(x, BLACK);
    }

    private static boolean colorOf(@Nullable Node<?> p) {
        return (p == null ? BLACK : p.color);
    }

    private static <T> @Nullable Node<T> parentOf(@Nullable Node<T> p) {
        return (p == null ? null : p.parent);
    }

    private static void setColor(@Nullable Node<?> p, boolean c) {
        if (p != null) p.color = c;
    }

    private static <T> @Nullable Node<T> leftOf(@Nullable Node<T> p) {
        return (p == null) ? null : p.left;
    }

    private static <T> @Nullable Node<T> rightOf(@Nullable Node<T> p) {
        return (p == null) ? null : p.right;
    }

    /**
     * Returns the successor of the specified Entry, or null if no such.
     */
    static <T> @Nullable Node<T> successor(@Nullable Node<T> t) {
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
    static <T> @Nullable Node<T> predecessor(@Nullable Node<T> t) {
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
        return this.size;
    }

    public boolean equals(Object other) {
        if (!(other instanceof IntervalTree<?> tree)) return false;
        if (this.size != tree.size) return false;
        return equals(this.root, tree.root);
    }

    private static boolean equals(@Nullable Node<?> node, @Nullable Node<?> o) {
        if (node == null && o == null) return true;
        if (node == null || o == null) return false;
        return node.equalsDown(o);
    }

    public int height() {
        return this.height(root);
    }

    public IntervalTree<T> copy() {
        return this.deepCopy(s -> s);
    }

    public IntervalTree<T> deepCopy(Function<T, T> copyFunction) {
        var tree = new IntervalTree<T>();
        tree.root = this.deepCopy(this.root, copyFunction);
        tree.size = this.size;
        return tree;
    }

    private @Nullable Node<T> deepCopy(@Nullable Node<T> origin, Function<T, T> copyFunction) {
        if (origin == null) return null;
        var node = this.copySingleWithoutLinks(origin, copyFunction);
        assert node != null;
        var left = this.deepCopy(origin.left, copyFunction);
        var right = this.deepCopy(origin.right, copyFunction);
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

    @Contract("null,_->null;_,_->_")
    private @Nullable Node<T> copySingleWithoutLinks(@Nullable Node<T> origin, Function<T, T> copyFunction) {
        if (origin == null) return null;
        var node = new Node<T>(0, 0, null, null);
        node.start = origin.start;
        node.maxEnd = origin.maxEnd;
        node.color = origin.color;
        for (var entry : origin.end.int2ObjectEntrySet()) {
            node.end.put(entry.getIntKey(), copyFunction.apply(entry.getValue()));
        }
        return node;
    }

    public int[] preOrder() {
        var list = new ArrayList<Integer>();
        this.preOrder(n -> list.add(n.start), root);
        return list.stream().mapToInt(i -> i).toArray();
    }

    public void preOrder(Consumer<Node<T>> consumer) {
        this.preOrder(consumer, this.root);
    }

    @SuppressWarnings("unchecked")
    public Node<T>[] inOrder() {
        var list = new ArrayList<Node<T>>();
        this.inOrder(list::add, this.root);
        return list.toArray(Node[]::new);
    }

    private void preOrder(Consumer<Node<T>> consumer, @Nullable Node<T> node) {
        if (node == null) return;
        consumer.accept(node);
        this.preOrder(consumer, node.left);
        this.preOrder(consumer, node.right);
    }

    private void inOrder(Consumer<Node<T>> consumer, @Nullable Node<T> node) {
        if (node == null) return;
        this.inOrder(consumer, node.left);
        consumer.accept(node);
        this.inOrder(consumer, node.right);
    }

    private int height(@Nullable Node<T> node) {
        if (node == null) return 0;
        return 1 + Math.max(this.height(node.left), this.height(node.right));
    }

    public static class Node<T> {
        public int start;
        public Int2ObjectRBTreeMap<@UnknownNullability T> end = new Int2ObjectRBTreeMap<>();
        public @Nullable Node<T> parent, left, right;
        public boolean color;
        public int maxEnd;

        public Node(int start, int end, @Nullable T data, @Nullable IntervalTree.Node<T> parent) {
            this.start = start;
            if (data != null) this.forceAdd(end, data);
            this.maxEnd = end;
            this.parent = parent;
        }

        private T forceAdd(int end, T data) {
            this.end.put(end, data);
            return data;
        }

        private boolean delete(int end) {
            return this.end.remove(end) != null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Node<?> other)) return false;
            return this.equalsDown(other) && this.equalsUp(other);
        }

        private boolean equalsUp(@Nullable Node<?> other) {
            if (other == null) return false;
            if (this.hasDifferingValues(other)) return false;
            if (this.parent == null && other.parent == null) return true;
            if (this.parent == null || other.parent == null) return false;
            return this.parent.equalsUp(other.parent);
        }

        private boolean equalsDown(@Nullable Node<?> other) {
            if (other == null) return false;
            if (this.hasDifferingValues(other)) return false;
            if (this.left != null) {
                if (!this.left.equalsDown(other.left)) return false;
            } else if (other.left != null) return false;
            if (this.right != null) {
                return this.right.equalsDown(other.right);
            } else return other.right == null;
        }

        private boolean hasDifferingValues(Node<?> other) {
            return this.start != other.start || !this.end.equals(other.end) || this.maxEnd != other.maxEnd || this.color != other.color;
        }

        private void resetMaxEnd() {
            this.maxEnd = this.calculateMaxEnd();
        }

        public int calculateMaxEnd() {
            var val = this.end.lastIntKey();
            if (this.left != null) {
                val = Math.max(val, this.left.maxEnd);
            }
            if (this.right != null) {
                val = Math.max(val, this.right.maxEnd);
            }
            return val;
        }

        private void replaceData(Node<T> other) {
            this.start = other.start;
            this.end = new Int2ObjectRBTreeMap<>(other.end);
        }

        static String toStringRecursive(@Nullable Node<?> node) {
            if (node == null) return "[nil]";
            return node.end.int2ObjectEntrySet().stream().map(e -> "[[%d,%d]=%s,left=%s,right=%s]".formatted(node.start, e.getIntKey(), e.getValue(), toStringRecursive(node.left), toStringRecursive(node.right))).collect(Collectors.joining(", "));
        }

        public String toStringRecursive() {
            return toStringRecursive(this);
        }

        @Override
        public String toString() {
            if (this.end.isEmpty()) return "[nil]";
            return this.end.int2ObjectEntrySet().stream().map(e -> "[%d,%d]=%s".formatted(start, e.getIntKey(), e.getValue())).collect(Collectors.joining(", "));
        }
    }
}
