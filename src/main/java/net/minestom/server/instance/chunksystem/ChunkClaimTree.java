package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.minestom.server.instance.chunksystem.ChunkClaim.Shape;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Utility data structure to store N entries (priority, shape) in a 2D interval tree (x,z)
 * <p>
 * Also contains utility methods to calculate the highest priority of any given position (x,z),
 * find all entries overlapping with any given position, and then some.
 */
public class ChunkClaimTree {
    private final IntervalTree<IntervalTree<Entries>> tree = new IntervalTree<>();
    // Reusable caches to reduce ArrayList allocations.
    private final ReusableList<IntervalTree.Node<IntervalTree<Entries>>> xCache = new ReusableList<>();
    private final ReusableList<IntervalTree.Node<Entries>> zCache = new ReusableList<>();
    private int size;

    /**
     * Inserts a region into the tree
     *
     * @param x        the centerX of the region
     * @param z        the centerZ of the region
     * @param size     the distance from the center to the edge. Differs per {@link Shape}
     * @param priority the load priority of the region
     * @param shape    the shape of the region
     */
    public void insert(int x, int z, int size, int priority, Shape shape) {
        this.insert(x - size, z - size, x + size, z + size, priority, shape);
    }

    /**
     * Inserts a region into the tree.
     * For a shape to be stored, the shape's bounding rectangle must be calculated
     *
     * @param minX     the minX of the region
     * @param minZ     the minZ of the region
     * @param maxX     the maxX of the region
     * @param maxZ     the maxZ of the region
     * @param priority the load priority of the region
     * @param shape    the shape of the region
     */
    public void insert(int minX, int minZ, int maxX, int maxZ, int priority, Shape shape) {
        var treeZ = this.tree.insertOrGet(minX, maxX, IntervalTree::new);
        var entries = treeZ.insertOrGet(minZ, maxZ, Entries::new);
        entries.add(new Entry(priority, shape));
        this.size++;
    }

    @ApiStatus.Internal
    public void insert(CompleteEntry entry) {
        this.insert(entry.minX, entry.minZ, entry.maxX, entry.maxZ, entry.entry.priority, entry.entry.shape);
    }

    public void delete(int x, int z, int size, int priority, Shape shape) {
        delete(x - size, z - size, x + size, z + size, priority, shape);
    }

    public void delete(int minX, int minZ, int maxX, int maxZ, int priority, Shape shape) {
        var treeZ = this.tree.get(minX, maxX);
        if (treeZ == null) throw new IllegalStateException("Tried to remove entry which was not in X tree");
        var entryList = treeZ.get(minZ, maxZ);
        if (entryList == null) throw new IllegalStateException("Tried to remove entry which was not in Z tree");
        entryList.remove(new Entry(priority, shape));
        if (entryList.isEmpty()) {
            if (!treeZ.delete(minZ, maxZ)) throw new IllegalStateException();
            if (treeZ.size() == 0) {
                if (!this.tree.delete(minX, maxX)) throw new IllegalStateException();
            }
        }
        this.size--;
    }

    @ApiStatus.Internal
    public void delete(CompleteEntry entry) {
        this.delete(entry.minX, entry.minZ, entry.maxX, entry.maxZ, entry.entry.priority, entry.entry.shape);
    }

    public int size() {
        return this.size;
    }

    @Override
    public String toString() {
        return this.tree.toString();
    }

    private void forEntries(int x, int z, Predicate<CompleteEntry> function) {
        this.tree.searchNodes(xCache, x);
        try {
            for (var nodeTreeX : xCache) {
                var minX = nodeTreeX.start;
                for (var treeXEntry : nodeTreeX.end.int2ObjectEntrySet()) {
                    var maxX = treeXEntry.getIntKey();
                    var widthX = maxX - minX;
                    var radiusX = widthX / 2;
                    var centerX = minX + radiusX;
                    var treeZ = treeXEntry.getValue();
                    treeZ.searchNodes(zCache, z);
                    try {
                        for (var nodeTreeZ : zCache) {
                            var minZ = nodeTreeZ.start;
                            for (var treeZEntry : nodeTreeZ.end.int2ObjectEntrySet()) {
                                var maxZ = treeZEntry.getIntKey();
                                var widthZ = maxZ - minZ;
                                var radiusZ = widthZ / 2;
                                var centerZ = minZ + radiusX;
                                var entries = treeZEntry.getValue();
                                for (var entry : entries) {
                                    var shape = entry.shape();
                                    if (shape.isInRadius(radiusX, radiusZ, centerX, centerZ, x, z)) {
                                        if (!function.test(new CompleteEntry(minX, minZ, maxX, maxZ, entry))) {
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    } finally {
                        zCache.reuseLastIterator();
                        zCache.clear();
                    }
                }
            }
        } finally {
            xCache.reuseLastIterator();
            xCache.clear();
        }
    }

    public void findEntries(ReusableList<CompleteEntry> result, int x, int z) {
        this.forEntries(x, z, e -> {
            result.add(e);
            return true;
        });
    }

    public List<CompleteEntry> findEntries(int x, int z) {
        var resultEntries = new ReusableList<CompleteEntry>();
        findEntries(resultEntries, x, z);
        return resultEntries.collect();
    }

    public CompleteEntry findHighestPriorityEntry(ReusableList<CompleteEntry> entries, PriorityDrop priorityDrop, int x, int z) {
        var comparator = entryPriorityComparator(priorityDrop, x, z);
        var stream = StreamSupport.stream(Spliterators.spliterator(entries.iterator(), entries.size(), 0), false);
        try {
            return stream.max(comparator).orElseThrow();
        } finally {
            entries.reuseLastIterator();
        }
    }

    /**
     * Calculates the priority of a given {@code entry} for a chunk at {@code x,z}
     */
    public double calculatePriority(PriorityDrop priorityDrop, CompleteEntry entry, int x, int z) {
        return this.calculatePriority(priorityDrop, entry.entry().priority, entry.centerX(), entry.centerZ(), x, z);
    }

    /**
     * Calculates the priority of a given {@code entry} for a chunk at {@code x,z}
     */
    public double calculatePriority(PriorityDrop priorityDrop, double priority, int claimX, int claimZ, int x, int z) {
        return priority - priorityDrop.calculate(claimX, claimZ, x, z);
    }

    public Comparator<ChunkClaimTree.CompleteEntry> entryPriorityComparator(PriorityDrop priorityDrop, int x, int z) {
        return Comparator.comparingDouble(e -> calculatePriority(priorityDrop, e, x, z));
    }

    public record Entries(
            Int2ObjectRBTreeMap<@UnknownNullability ArrayList<Entry>> byPriority) implements Iterable<Entry> {
        private Entries() {
            this(new Int2ObjectRBTreeMap<>(Comparator.reverseOrder()));
        }

        public boolean isEmpty() {
            return this.byPriority.isEmpty();
        }

        public void add(Entry entry) {
            this.byPriority.computeIfAbsent(entry.priority, _ -> new ArrayList<>(4)).add(entry);
        }

        public void remove(Entry entry) {
            var list = this.byPriority.get(entry.priority);
            if (list != null) {
                list.remove(entry);
                if (list.isEmpty()) {
                    this.byPriority.remove(entry.priority);
                }
            }
        }

        @Override
        public String toString() {
            return this.byPriority.values().stream().flatMap(Collection::stream).map(Entry::toString).collect(Collectors.joining(", "));
        }

        @Override
        public Iterator<Entry> iterator() {
            return new It(this.byPriority.values().iterator());
        }

        private static class It implements Iterator<Entry> {
            private final Iterator<ArrayList<Entry>> it;
            private @Nullable Iterator<Entry> it2 = null;

            public It(Iterator<ArrayList<Entry>> it) {
                this.it = it;
            }

            @Override
            public boolean hasNext() {
                if (this.it2 == null || !this.it2.hasNext()) next0();
                return this.it2 != null && this.it2.hasNext();
            }

            private void next0() {
                if (!this.it.hasNext()) return;
                var list = this.it.next();
                this.it2 = list.iterator();
            }

            @Override
            public Entry next() {
                if (this.it2 == null || !this.it2.hasNext()) next0();
                if (this.it2 == null || !this.it2.hasNext()) throw new NoSuchElementException();
                return this.it2.next();
            }
        }
    }

    public record CompleteEntry(int minX, int minZ, int maxX, int maxZ, Entry entry) {
        public int centerX() {
            return minX + (maxX - minX) / 2;
        }

        public int centerZ() {
            return minZ + (maxZ - minZ) / 2;
        }
    }

    public record Entry(int priority, Shape shape) {
    }
}
