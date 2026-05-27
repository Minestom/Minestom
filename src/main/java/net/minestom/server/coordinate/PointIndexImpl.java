package net.minestom.server.coordinate;

import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.IntConsumer;

import static net.minestom.server.coordinate.CoordConversion.SECTION_SIZE;
import static net.minestom.server.coordinate.CoordConversion.chunkIndex;
import static net.minestom.server.coordinate.CoordConversion.globalToChunk;

final class PointIndexImpl implements PointIndex {

    private static final int INITIAL_BUCKET_CAPACITY = 4;
    private static final double SECTION_SIZE_D = SECTION_SIZE;

    private final Int2ObjectOpenHashMap<@Nullable Slot> byId = new Int2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<@Nullable Bucket> byChunk = new Long2ObjectOpenHashMap<>();
    private final IntCollection view = new ValuesView();

    @Override
    public void add(int id, Point point) {
        if (byId.get(id) != null) {
            throw new IllegalStateException("Point with id " + id + " already present");
        }
        final double x = point.x(), y = point.y(), z = point.z();
        final long chunk = chunkIndex(globalToChunk(x), globalToChunk(z));

        Bucket bucket = byChunk.get(chunk);
        if (bucket == null) {
            bucket = new Bucket();
            byChunk.put(chunk, bucket);
        }
        final int slotIndex = bucket.add(id, x, y, z);
        byId.put(id, new Slot(chunk, slotIndex, point));
    }

    @Override
    public @Nullable Point remove(int id) {
        final Slot slot = byId.remove(id);
        if (slot == null) return null;
        final long chunk = slot.chunk;
        final Point point = slot.point;
        final Bucket bucket = byChunk.get(chunk);
        assert bucket != null;
        final int swappedId = bucket.removeAt(slot.slotIndex);
        if (swappedId != -1) {
            final Slot swappedSlot = byId.get(swappedId);
            assert swappedSlot != null;
            swappedSlot.slotIndex = slot.slotIndex;
        }
        if (bucket.size == 0) byChunk.remove(chunk);

        return point;
    }

    @Override
    public @Nullable Point move(int id, Point newPoint) {
        final Slot slot = byId.get(id);
        if (slot == null) return null;
        final Point oldPoint = slot.point;
        final long oldChunk = slot.chunk;
        final double newX = newPoint.x(), newY = newPoint.y(), newZ = newPoint.z();
        final long newChunk = chunkIndex(globalToChunk(newX), globalToChunk(newZ));

        if (oldChunk == newChunk) {
            final Bucket bucket = byChunk.get(oldChunk);
            assert bucket != null;
            bucket.update(slot.slotIndex, newX, newY, newZ);
            slot.point = newPoint;
            return oldPoint;
        }

        Bucket newBucket = byChunk.get(newChunk);
        if (newBucket == null) {
            newBucket = new Bucket();
            byChunk.put(newChunk, newBucket);
        }
        final int newSlotIndex = newBucket.add(id, newX, newY, newZ);
        final Bucket oldBucket = byChunk.get(oldChunk);
        assert oldBucket != null;
        final int swappedId = oldBucket.removeAt(slot.slotIndex);
        if (swappedId != -1) {
            final Slot swappedSlot = byId.get(swappedId);
            assert swappedSlot != null;
            swappedSlot.slotIndex = slot.slotIndex;
        }
        if (oldBucket.size == 0) byChunk.remove(oldChunk);
        slot.chunk = newChunk;
        slot.slotIndex = newSlotIndex;
        slot.point = newPoint;
        return oldPoint;
    }

    @Override
    public @Nullable Point get(int id) {
        final Slot slot = byId.get(id);
        return slot == null ? null : slot.point;
    }

    @Override
    public int size() {
        return byId.size();
    }

    @Override
    public @Unmodifiable IntCollection all() {
        return view;
    }

    @Override
    public int nearest(Point point) {
        if (byChunk.isEmpty()) return -1;
        final double px = point.x(), py = point.y(), pz = point.z();
        final NearestState state = new NearestState();

        if (byChunk.size() <= 4) {
            for (Bucket b : byChunk.values()) scanBucketForNearest(b, px, py, pz, state);
            return state.bestId;
        }

        final int qcx = globalToChunk(px), qcz = globalToChunk(pz);
        final int maxRing = 4096;
        for (int r = 0; r <= maxRing; r++) {
            if (r >= 1) {
                final double minPossibleDist = SECTION_SIZE_D * (r - 1);
                if (state.bestSq < minPossibleDist * minPossibleDist) break;
            }
            if (r == 0) {
                scanChunkForNearest(qcx, qcz, px, py, pz, state);
            } else {
                for (int dx = -r; dx <= r; dx++) {
                    scanChunkForNearest(qcx + dx, qcz - r, px, py, pz, state);
                    scanChunkForNearest(qcx + dx, qcz + r, px, py, pz, state);
                }
                for (int dz = -r + 1; dz <= r - 1; dz++) {
                    scanChunkForNearest(qcx - r, qcz + dz, px, py, pz, state);
                    scanChunkForNearest(qcx + r, qcz + dz, px, py, pz, state);
                }
            }
        }
        return state.bestId;
    }

    private void scanChunkForNearest(int cx, int cz, double px, double py, double pz, NearestState state) {
        final double cxMin = cx * SECTION_SIZE_D, cxMax = cxMin + SECTION_SIZE_D;
        final double czMin = cz * SECTION_SIZE_D, czMax = czMin + SECTION_SIZE_D;
        final double dx = Math.max(0.0, Math.max(cxMin - px, px - cxMax));
        final double dz = Math.max(0.0, Math.max(czMin - pz, pz - czMax));
        if (dx * dx + dz * dz >= state.bestSq) return;
        final Bucket b = byChunk.get(chunkIndex(cx, cz));
        if (b != null) scanBucketForNearest(b, px, py, pz, state);
    }

    private void scanBucketForNearest(Bucket b, double px, double py, double pz, NearestState state) {
        final int n = b.size;
        final double[] xs = b.xs, ys = b.ys, zs = b.zs;
        final int[] ids = b.ids;
        double bestSq = state.bestSq;
        int bestId = state.bestId;
        for (int i = 0; i < n; i++) {
            final double dx = xs[i] - px, dy = ys[i] - py, dz = zs[i] - pz;
            final double d = dx * dx + dy * dy + dz * dz;
            if (d < bestSq) {
                bestSq = d;
                bestId = ids[i];
            }
        }
        state.bestSq = bestSq;
        state.bestId = bestId;
    }

    @Override
    public void forEachWithin(Point point, double radius, IntConsumer consumer) {
        final double px = point.x(), py = point.y(), pz = point.z();
        final double rSq = radius * radius;
        final int minCX = globalToChunk(px - radius);
        final int maxCX = globalToChunk(px + radius);
        final int minCZ = globalToChunk(pz - radius);
        final int maxCZ = globalToChunk(pz + radius);

        if (minCX == maxCX && minCZ == maxCZ) {
            final Bucket b = byChunk.get(chunkIndex(minCX, minCZ));
            if (b == null) return;
            forEachInBucketWithin(b, px, py, pz, rSq, consumer);
            return;
        }
        for (int cx = minCX; cx <= maxCX; cx++) {
            final double cxMin = cx * SECTION_SIZE_D, cxMax = cxMin + SECTION_SIZE_D;
            final double cdx = Math.max(0.0, Math.max(cxMin - px, px - cxMax));
            final double cdxSq = cdx * cdx;
            if (cdxSq >= rSq) continue;
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                final double czMin = cz * SECTION_SIZE_D, czMax = czMin + SECTION_SIZE_D;
                final double cdz = Math.max(0.0, Math.max(czMin - pz, pz - czMax));
                if (cdxSq + cdz * cdz >= rSq) continue;
                final Bucket b = byChunk.get(chunkIndex(cx, cz));
                if (b == null) continue;
                forEachInBucketWithin(b, px, py, pz, rSq, consumer);
            }
        }
    }

    private static void forEachInBucketWithin(Bucket b, double px, double py, double pz,
                                              double rSq, IntConsumer consumer) {
        final int n = b.size;
        final double[] xs = b.xs, ys = b.ys, zs = b.zs;
        final int[] ids = b.ids;
        for (int i = 0; i < n; i++) {
            final double dx = xs[i] - px, dy = ys[i] - py, dz = zs[i] - pz;
            if (dx * dx + dy * dy + dz * dz <= rSq) consumer.accept(ids[i]);
        }
    }

    @Override
    public int count(Point point, double radius) {
        final double px = point.x(), py = point.y(), pz = point.z();
        final double rSq = radius * radius;
        final int minCX = globalToChunk(px - radius);
        final int maxCX = globalToChunk(px + radius);
        final int minCZ = globalToChunk(pz - radius);
        final int maxCZ = globalToChunk(pz + radius);
        if (minCX == maxCX && minCZ == maxCZ) {
            final Bucket b = byChunk.get(chunkIndex(minCX, minCZ));
            return b == null ? 0 : countInBucketWithin(b, px, py, pz, rSq);
        }
        int total = 0;
        for (int cx = minCX; cx <= maxCX; cx++) {
            final double cxMin = cx * SECTION_SIZE_D, cxMax = cxMin + SECTION_SIZE_D;
            final double cdx = Math.max(0.0, Math.max(cxMin - px, px - cxMax));
            final double cdxSq = cdx * cdx;
            if (cdxSq >= rSq) continue;
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                final double czMin = cz * SECTION_SIZE_D, czMax = czMin + SECTION_SIZE_D;
                final double cdz = Math.max(0.0, Math.max(czMin - pz, pz - czMax));
                if (cdxSq + cdz * cdz >= rSq) continue;
                final Bucket b = byChunk.get(chunkIndex(cx, cz));
                if (b == null) continue;
                total += countInBucketWithin(b, px, py, pz, rSq);
            }
        }
        return total;
    }

    private static int countInBucketWithin(Bucket b, double px, double py, double pz, double rSq) {
        final int n = b.size;
        final double[] xs = b.xs, ys = b.ys, zs = b.zs;
        int total = 0;
        for (int i = 0; i < n; i++) {
            final double dx = xs[i] - px, dy = ys[i] - py, dz = zs[i] - pz;
            if (dx * dx + dy * dy + dz * dz <= rSq) total++;
        }
        return total;
    }

    @Override
    public void forEachInChunk(int chunkX, int chunkZ, IntConsumer consumer) {
        final Bucket b = byChunk.get(chunkIndex(chunkX, chunkZ));
        if (b == null) return;
        forEachInBucket(b, consumer);
    }

    private static void forEachInBucket(Bucket b, IntConsumer consumer) {
        final int n = b.size;
        final int[] ids = b.ids;
        for (int i = 0; i < n; i++) consumer.accept(ids[i]);
    }

    @Override
    public @Unmodifiable IntCollection inChunk(int chunkX, int chunkZ) {
        return new ChunkView(this, chunkIndex(chunkX, chunkZ));
    }

    @Override
    public void forEachInChunkRange(Point point, int chunkRange, IntConsumer consumer) {
        final int centerX = point.chunkX();
        final int centerZ = point.chunkZ();
        for (int cx = centerX - chunkRange; cx <= centerX + chunkRange; cx++) {
            for (int cz = centerZ - chunkRange; cz <= centerZ + chunkRange; cz++) {
                final Bucket b = byChunk.get(chunkIndex(cx, cz));
                if (b == null) continue;
                forEachInBucket(b, consumer);
            }
        }
    }

    @Override
    public void forEachInChunkRangeDiffering(Point oldPoint, Point newPoint, int chunkRange,
                                             IntConsumer added, IntConsumer removed) {
        final int oldCX = oldPoint.chunkX(), oldCZ = oldPoint.chunkZ();
        final int newCX = newPoint.chunkX(), newCZ = newPoint.chunkZ();
        for (int cx = newCX - chunkRange; cx <= newCX + chunkRange; cx++) {
            for (int cz = newCZ - chunkRange; cz <= newCZ + chunkRange; cz++) {
                if (Math.abs(cx - oldCX) <= chunkRange && Math.abs(cz - oldCZ) <= chunkRange) continue;
                final Bucket b = byChunk.get(chunkIndex(cx, cz));
                if (b == null) continue;
                forEachInBucket(b, added);
            }
        }
        for (int cx = oldCX - chunkRange; cx <= oldCX + chunkRange; cx++) {
            for (int cz = oldCZ - chunkRange; cz <= oldCZ + chunkRange; cz++) {
                if (Math.abs(cx - newCX) <= chunkRange && Math.abs(cz - newCZ) <= chunkRange) continue;
                final Bucket b = byChunk.get(chunkIndex(cx, cz));
                if (b == null) continue;
                forEachInBucket(b, removed);
            }
        }
    }

    private static final class NearestState {
        int bestId = -1;
        double bestSq = Double.POSITIVE_INFINITY;
    }

    private static final class Slot {
        long chunk;
        int slotIndex;
        Point point;

        Slot(long chunk, int slotIndex, Point point) {
            this.chunk = chunk;
            this.slotIndex = slotIndex;
            this.point = point;
        }
    }

    private static final class Bucket {
        int size;
        double[] xs, ys, zs;
        int[] ids;

        Bucket() {
            this.xs = new double[INITIAL_BUCKET_CAPACITY];
            this.ys = new double[INITIAL_BUCKET_CAPACITY];
            this.zs = new double[INITIAL_BUCKET_CAPACITY];
            this.ids = new int[INITIAL_BUCKET_CAPACITY];
        }

        int add(int id, double x, double y, double z) {
            if (size == xs.length) grow();
            final int i = size++;
            xs[i] = x;
            ys[i] = y;
            zs[i] = z;
            ids[i] = id;
            return i;
        }

        void update(int idx, double x, double y, double z) {
            xs[idx] = x;
            ys[idx] = y;
            zs[idx] = z;
        }

        int removeAt(int idx) {
            final int last = --size;
            if (idx == last) return -1;
            xs[idx] = xs[last];
            ys[idx] = ys[last];
            zs[idx] = zs[last];
            ids[idx] = ids[last];
            return ids[idx];
        }

        private void grow() {
            final int newCap = xs.length * 2;
            xs = Arrays.copyOf(xs, newCap);
            ys = Arrays.copyOf(ys, newCap);
            zs = Arrays.copyOf(zs, newCap);
            ids = Arrays.copyOf(ids, newCap);
        }
    }

    private final class ValuesView extends AbstractIntCollection {
        @Override
        public IntIterator iterator() {
            final var it = byId.keySet().iterator();
            return new IntIterator() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public int nextInt() {
                    return it.nextInt();
                }
            };
        }

        @Override
        public int size() {
            return byId.size();
        }
    }

    private static final class ChunkView extends AbstractIntCollection {
        private final PointIndexImpl index;
        private final long chunkKey;

        ChunkView(PointIndexImpl index, long chunkKey) {
            this.index = index;
            this.chunkKey = chunkKey;
        }

        @Override
        public IntIterator iterator() {
            final Bucket b = index.byChunk.get(chunkKey);
            if (b == null) return IntIterators.EMPTY_ITERATOR;
            final int[] ids = b.ids;
            final int n = b.size;
            return new IntIterator() {
                int i = 0;

                @Override
                public boolean hasNext() {
                    return i < n;
                }

                @Override
                public int nextInt() {
                    if (i >= n) throw new NoSuchElementException();
                    return ids[i++];
                }
            };
        }

        @Override
        public int size() {
            final Bucket b = index.byChunk.get(chunkKey);
            return b == null ? 0 : b.size;
        }

        @Override
        public boolean isEmpty() {
            final Bucket b = index.byChunk.get(chunkKey);
            return b == null || b.size == 0;
        }
    }

}
