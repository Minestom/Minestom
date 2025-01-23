package net.minestom.server.instance.chunksystem.impl;

import net.minestom.server.instance.chunksystem.ChunkClaim;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeMap;

public final class QuadTree {
    /**
     * center x,y
     */
    private final int x, y;
    private final int radius;
    private final int capacity;
    private final @Nullable QuadTree parent;
    private final Entry[] elements;
    private final Leaf leaf;
    private int elementCount;
    private int totalCount;
    private boolean divided = false;
    private QuadTree nw, ne, sw, se;

    public QuadTree(int x, int y, int radius, int capacity) {
        this(x, y, radius, capacity, null);
    }

    public QuadTree(int x, int y, int radius, int capacity, @Nullable QuadTree parent) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.capacity = capacity;
        this.parent = parent;
        this.elements = radius == 0 ? null : new Entry[capacity];
        this.elementCount = 0;
        this.leaf = radius == 0 ? new Leaf() : null;
    }

    @Nullable
    public ChunkClaim highestPriorityClaim(int x, int y) {
        if (!contains(x, y)) return null;
        if (leaf != null) {
            return leaf.claims.isEmpty() ? null : leaf.claims.firstKey();
        }
        ChunkClaim highest = null;
        for (var i = 0; i < this.elementCount; i++) {
            var element = this.elements[i];
            if (!isInEntry(element, x, y)) continue;
            if (highest == null || highest.priority() < element.claim.priority()) {
                highest = element.claim;
            }
        }
        if (divided) {
            var nwh = nw.highestPriorityClaim(x, y);
            var neh = ne.highestPriorityClaim(x, y);
            var swh = sw.highestPriorityClaim(x, y);
            var seh = se.highestPriorityClaim(x, y);
            var c = firstNonNull(nwh, neh, swh, seh);
            if (c != null && (highest == null || highest.priority() < c.priority())) highest = c;
        }
        return highest;
    }

    private static @Nullable ChunkClaim firstNonNull(@Nullable ChunkClaim c1, @Nullable ChunkClaim c2, @Nullable ChunkClaim c3, @Nullable ChunkClaim c4) {
        if (c1 != null) return c1;
        if (c2 != null) return c2;
        if (c3 != null) return c3;
        return c4;
    }

    private boolean isInEntry(Entry entry, int x, int y) {
        var radius = entry.claim.radius();
        var cx = entry.x;
        var cy = entry.y;

        if (cx - radius > x) return false;
        if (cx + radius < x) return false;
        if (cy - radius > y) return false;
        if (cy + radius < y) return false;

        return entry.claim.shape().isInRadius(radius, radius * radius, cx, cy, x, y);
    }

    public void insert(int x, int y, ChunkClaim claim) {
        if (!insert0(new Entry(x, y, claim))) {
            throw new IllegalStateException("QuadTree[" + this.x + "," + this.y + "][" + this.radius + "] didn't accept claim at " + x + "," + y);
        }
    }

    public void remove(int x, int y, ChunkClaim claim) {
        if (!remove0(new Entry(x, y, claim))) {
            throw new IllegalStateException("QuadTree[" + this.x + "," + this.y + "][" + this.radius + "] didn't remove claim at " + x + "," + y);
        }
    }

    private boolean remove0(Entry entry) {
        if (!contains(x, y)) return false;
        if (leaf != null) {
            totalCount--;
            return leaf.remove(entry.claim);
        }
        var removed = false;
        for (var i = 0; i < this.elementCount; i++) {
            var element = this.elements[i];
            if (removed) {
                this.elements[i - 1] = element;
                this.elements[i] = null;
                continue;
            }
            if (element.equals(entry)) {
                removed = true;
                elements[i] = null;
            }
        }
        if (!removed) {
            removed = nw.remove0(entry) || ne.remove0(entry) || sw.remove0(entry) || se.remove0(entry);
        }
        if (removed) {
            this.elementCount--;
            this.totalCount--;
        } else return false;
        if (totalCount == capacity) {
            recombine();
        }
        return true;
    }

    private boolean insert0(Entry entry) {
        if (!contains(x, y)) return false;
        if (leaf != null) {
            totalCount++;
            return leaf.insert(entry.claim);
        }
        if (capacity == elementCount) {
            subdivide();
        }
        if (divided) {
            var r = nw.insert0(entry) || ne.insert0(entry) || sw.insert0(entry) || se.insert0(entry);
            if (r) totalCount++;
            return r;
        }
        elements[elementCount++] = entry;
        totalCount++;
        return true;
    }

    private void recombine() {
        collect(nw);
        collect(ne);
        collect(sw);
        collect(se);
        nw = ne = sw = se = null;
        divided = false;
    }

    private void collect(QuadTree tree) {
        for (var i = 0; i < tree.elementCount; i++) {
            this.elements[this.elementCount++] = tree.elements[i];
        }
    }

    private void subdivide() {
        if (radius > 1) {
            nw = new QuadTree(x - radius / 2, y + radius / 2, radius, capacity, this);
            ne = new QuadTree(x + radius / 2, y + radius / 2, radius, capacity, this);
            sw = new QuadTree(x - radius / 2, y - radius / 2, radius, capacity, this);
            se = new QuadTree(x + radius / 2, y - radius / 2, radius, capacity, this);
        } else if (radius == 1) {
            nw = new QuadTree(x - 1, y + 1, 0, capacity, this);
            ne = new QuadTree(x + 1, y + 1, 0, capacity, this);
            sw = new QuadTree(x - 1, y - 1, 0, capacity, this);
            se = new QuadTree(x + 1, y - 1, 0, capacity, this);
        } else throw new IllegalStateException();
        divided = true;
    }

    private boolean contains(int x, int y) {
        if (this.leaf != null) return this.x == x && this.y == y;
        return this.x - this.radius <= x && this.y - this.radius <= y && this.x + this.radius > x && this.y + this.radius > y;
    }

    private static class Leaf {
        private final TreeMap<ChunkClaim, Integer> claims = new TreeMap<>(Comparator.comparing(ChunkClaim::priority).reversed());

        public boolean insert(ChunkClaim claim) {
            claims.compute(claim, (c, i) -> i == null ? 1 : i + 1);
            return true;
        }

        public boolean remove(ChunkClaim claim) {
            claims.compute(claim, (c, i) -> {
                if (i == null) throw new NullPointerException();
                if (i == 1) return null;
                return i - 1;
            });
            return true;
        }
    }

    private record Entry(int x, int y, ChunkClaim claim) {
    }
}
