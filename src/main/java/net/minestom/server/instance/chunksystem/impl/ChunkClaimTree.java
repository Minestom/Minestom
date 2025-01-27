package net.minestom.server.instance.chunksystem.impl;

import net.minestom.server.instance.chunksystem.ChunkClaim.Shape;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility data structure to store N entries (priority, shape) in a 2D interval tree (x,z)
 * <p>
 * Also contains utility methods to calculate the highest priority of any given position (x,z),
 * find all entries overlapping with any given position, and then some.
 */
public class ChunkClaimTree {
    private final IntervalTree<IntervalTree<ArrayList<Entry>>> tree = new IntervalTree<>();
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
     * Inputs must be a square, because this data structure only supports storing squares.
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
        var entryList = treeZ.insertOrGet(minZ, maxZ, ArrayList::new);
        entryList.add(new Entry(priority, shape));
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
        return size;
    }

    public List<CompleteEntry> findEntries(int x, int z) {
        var resultEntries = new ArrayList<CompleteEntry>(1);
        var nodesTreeX = this.tree.searchNodes(x);
        for (var nodeTreeX : nodesTreeX) {
            var minX = nodeTreeX.start;
            for (var treeXEntry : nodeTreeX.end.entrySet()) {
                var maxX = treeXEntry.getKey();
                var widthX = maxX - minX;
                var radiusX = widthX / 2;
                var radiusSqX = -1;
                var centerX = minX + radiusX;
                var treeZ = treeXEntry.getValue();
                var nodesTreeZ = treeZ.searchNodes(z);
                for (var nodeTreeZ : nodesTreeZ) {
                    var minZ = nodeTreeZ.start;
                    for (var treeZEntry : nodeTreeZ.end.entrySet()) {
                        var maxZ = treeZEntry.getKey();
                        var widthZ = maxZ - minZ;
                        var radiusZ = widthZ / 2;
                        var radiusSqZ = radiusZ * radiusZ;
                        var centerZ = minZ + radiusX;
                        var entries = treeZEntry.getValue();
                        for (var entry : entries) {
                            var shape = entry.shape();
                            var inX = radiusX;
                            var inZ = radiusZ;
                            if (shape.doesWantSquared()) {
                                inX = radiusSqX;
                                if (inX == -1) inX = radiusSqX = radiusX * radiusX;
                                inZ = radiusSqZ;
                                if (inZ == -1) inZ = radiusSqZ = radiusZ * radiusZ;
                            }
                            if (shape.isInRadius(inX, inZ, centerX, centerZ, x, z)) {
                                resultEntries.add(new CompleteEntry(minX, minZ, maxX, maxZ, entry));
                            }
                        }
                    }
                }
            }
        }
        return resultEntries;
    }

    public record CompleteEntry(int minX, int minZ, int maxX, int maxZ, Entry entry) {
    }

    public record Entry(int priority, Shape shape) {
    }
}
