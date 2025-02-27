package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.minestom.server.instance.chunksystem.ChunkClaim.Shape;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility data structure to store N entries (priority, shape) in a 2D interval tree (x,z)
 * <p>
 * Also contains utility methods to calculate the highest priority of any given position (x,z),
 * find all entries overlapping with any given position, and then some.
 */
public class ChunkClaimTree {
    private final IntervalTree<IntervalTree<Entries>> tree = new IntervalTree<>();
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
    public void insert(int x, int z, int size, int priority, @NotNull Shape shape) {
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
    public void insert(int minX, int minZ, int maxX, int maxZ, int priority, @NotNull Shape shape) {
        var treeZ = this.tree.insertOrGet(minX, maxX, IntervalTree::new);
        var entries = treeZ.insertOrGet(minZ, maxZ, Entries::new);
        entries.add(new Entry(priority, shape));
        this.size++;
    }

    @ApiStatus.Internal
    public void insert(@NotNull CompleteEntry entry) {
        this.insert(entry.minX, entry.minZ, entry.maxX, entry.maxZ, entry.entry.priority, entry.entry.shape);
    }

    public void delete(int x, int z, int size, int priority, @NotNull Shape shape) {
        delete(x - size, z - size, x + size, z + size, priority, shape);
    }

    public void delete(int minX, int minZ, int maxX, int maxZ, int priority, @NotNull Shape shape) {
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
    public void delete(@NotNull CompleteEntry entry) {
        this.delete(entry.minX, entry.minZ, entry.maxX, entry.maxZ, entry.entry.priority, entry.entry.shape);
    }

    public int size() {
        return this.size;
    }

    @Override
    public String toString() {
        return this.tree.toString();
    }

    private boolean forEntries(int x, int z, Predicate<CompleteEntry> function) {
        var nodesTreeX = this.tree.searchNodes(x);
        for (var nodeTreeX : nodesTreeX) {
            var minX = nodeTreeX.start;
            for (var treeXEntry : nodeTreeX.end.entrySet()) {
                var maxX = treeXEntry.getKey();
                var widthX = maxX - minX;
                var radiusX = widthX / 2;
                var centerX = minX + radiusX;
                var treeZ = treeXEntry.getValue();
                var nodesTreeZ = treeZ.searchNodes(z);
                for (var nodeTreeZ : nodesTreeZ) {
                    var minZ = nodeTreeZ.start;
                    for (var treeZEntry : nodeTreeZ.end.entrySet()) {
                        var maxZ = treeZEntry.getKey();
                        var widthZ = maxZ - minZ;
                        var radiusZ = widthZ / 2;
                        var centerZ = minZ + radiusX;
                        var entries = treeZEntry.getValue();
                        for (var entry : entries) {
                            var shape = entry.shape();
                            if (shape.isInRadius(radiusX, radiusZ, centerX, centerZ, x, z)) {
                                if (!function.test(new CompleteEntry(minX, minZ, maxX, maxZ, entry))) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean noEntries(int x, int z) {
        return this.forEntries(x, z, e -> false);
    }

    public List<CompleteEntry> findEntries(int x, int z) {
        var resultEntries = new ArrayList<CompleteEntry>(1);
        this.forEntries(x, z, e -> {
            resultEntries.add(e);
            return true;
        });
        return resultEntries;
    }

    public @Nullable CompleteEntry findHighestPriorityEntry(@NotNull PriorityDrop priorityDrop, int x, int z) {
        var entries = this.findEntries(x, z);
        if (entries.isEmpty()) return null;
        return findHighestPriorityEntry(entries, priorityDrop, x, z);
    }

    public @NotNull CompleteEntry findHighestPriorityEntry(@NotNull List<CompleteEntry> entries, @NotNull PriorityDrop priorityDrop, int x, int z) {
        var comparator = entryPriorityComparator(priorityDrop, x, z);
        return entries.stream().max(comparator).orElseThrow();
    }

    /**
     * Calculates the priority of a given {@code entry} for a chunk at {@code x,z}
     */
    public double calculatePriority(@NotNull PriorityDrop priorityDrop, @NotNull CompleteEntry entry, int x, int z) {
        return this.calculatePriority(priorityDrop, entry.entry().priority, entry.centerX(), entry.centerZ(), x, z);
    }

    /**
     * Calculates the priority of a given {@code entry} for a chunk at {@code x,z}
     */
    public double calculatePriority(@NotNull PriorityDrop priorityDrop, double priority, int claimX, int claimZ, int x, int z) {
        return priority - priorityDrop.calculate(claimX, claimZ, x, z);
    }

    public Comparator<ChunkClaimTree.CompleteEntry> entryPriorityComparator(@NotNull PriorityDrop priorityDrop, int x, int z) {
        return Comparator.comparingDouble(e -> calculatePriority(priorityDrop, e, x, z));
    }

    public record Entries(Int2ObjectRBTreeMap<ArrayList<Entry>> byPriority) implements Iterable<Entry> {
        private Entries() {
            this(new Int2ObjectRBTreeMap<>(Comparator.reverseOrder()));
        }

        public boolean isEmpty() {
            return this.byPriority.isEmpty();
        }

        public void add(Entry entry) {
            this.byPriority.computeIfAbsent(entry.priority, prio -> new ArrayList<>(4)).add(entry);
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
        public @NotNull Iterator<Entry> iterator() {
            return new It(this.byPriority.values().iterator());
        }

        private static class It implements Iterator<Entry> {
            private final Iterator<ArrayList<Entry>> it;
            private Iterator<Entry> it2 = null;

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

    public record CompleteEntry(int minX, int minZ, int maxX, int maxZ, @NotNull Entry entry) {
        public int centerX() {
            return minX + (maxX - minX) / 2;
        }

        public int centerZ() {
            return minZ + (maxZ - minZ) / 2;
        }
    }

    public record Entry(int priority, @NotNull Shape shape) {
    }
}
