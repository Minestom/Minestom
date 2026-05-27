package net.minestom.server.coordinate;

import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import space.vectrix.flare.fastutil.Int2ObjectSyncMap;
import space.vectrix.flare.fastutil.Long2ObjectSyncMap;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.IntConsumer;

import static net.minestom.server.coordinate.CoordConversion.*;

final class PointIndexConcurrentImpl implements PointIndex {

    private static final int LOCK_STRIPES = 64;
    private static final int LOCK_MASK = LOCK_STRIPES - 1;
    private static final double SECTION_SIZE_D = SECTION_SIZE;

    private final Int2ObjectSyncMap<@Nullable Slot> byId = Int2ObjectSyncMap.hashmap();
    private final Long2ObjectSyncMap<@Nullable Object> byChunk = Long2ObjectSyncMap.hashmap();
    private final Object[] locks;
    private final Object crossChunkLock = new Object();
    private final IntCollection view = new ValuesView();

    PointIndexConcurrentImpl() {
        this.locks = new Object[LOCK_STRIPES];
        for (int i = 0; i < LOCK_STRIPES; i++) this.locks[i] = new Object();
    }

    private static int stripeFor(long chunk) {
        long hash = chunk;
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= hash >>> 33;
        return (int) hash & LOCK_MASK;
    }

    private Object lockFor(long chunk) {
        return locks[stripeFor(chunk)];
    }

    private @Nullable Bucket bucket(long chunkKey) {
        final Object value = byChunk.get(chunkKey);
        return value instanceof Bucket b ? b : null;
    }

    private boolean belongsToChunk(int id, long chunk) {
        final Slot slot = byId.get(id);
        return slot != null && slot.chunk == chunk;
    }

    @Override
    public void add(int id, Point point) {
        final double x = point.x(), y = point.y(), z = point.z();
        final long chunk = chunkIndex(globalToChunk(x), globalToChunk(z));
        synchronized (crossChunkLock) {
            final Slot previous = byId.putIfAbsent(id, new Slot(chunk, point));
            if (previous != null) {
                throw new IllegalStateException("Point with id " + id + " already present");
            }
            synchronized (lockFor(chunk)) {
                Bucket bucket = bucket(chunk);
                if (bucket == null) bucket = Bucket.first(id, x, y, z);
                else bucket = bucket.withAdded(id, x, y, z);
                byChunk.put(chunk, bucket);
            }
        }
    }

    @Override
    public @Nullable Point remove(int id) {
        final Slot removed;
        synchronized (crossChunkLock) {
            removed = byId.remove(id);
            if (removed == null) return null;
            synchronized (lockFor(removed.chunk)) {
                final Bucket bucket = bucket(removed.chunk);
                if (bucket != null) {
                    final int idx = bucket.indexOf(id);
                    if (idx != -1) {
                        final Bucket newBucket = bucket.withRemovedAt(idx);
                        if (newBucket == null) byChunk.remove(removed.chunk);
                        else byChunk.put(removed.chunk, newBucket);
                    }
                }
            }
        }
        return removed.point;
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    @Override
    public @Nullable Point move(int id, Point newPoint) {
        final Slot slot = byId.get(id);
        if (slot == null) return null;
        final long oldChunk = slot.chunk;
        final Point oldPoint = slot.point;
        final double newX = newPoint.x(), newY = newPoint.y(), newZ = newPoint.z();
        final long newChunk = chunkIndex(globalToChunk(newX), globalToChunk(newZ));

        if (oldChunk == newChunk) {
            synchronized (lockFor(oldChunk)) {
                final Bucket bucket = bucket(oldChunk);
                if (bucket != null) {
                    final int idx = bucket.indexOf(id);
                    if (idx != -1) byChunk.put(oldChunk, bucket.withUpdated(idx, newX, newY, newZ));
                }
                byId.put(id, new Slot(oldChunk, newPoint));
            }
            return oldPoint;
        }

        final int oldStripe = stripeFor(oldChunk);
        final int newStripe = stripeFor(newChunk);
        synchronized (crossChunkLock) {
            if (oldStripe == newStripe) {
                synchronized (locks[oldStripe]) {
                    doMoveCross(id, oldChunk, newChunk, newX, newY, newZ, newPoint);
                }
            } else {
                final Object firstLock = locks[Math.min(oldStripe, newStripe)];
                final Object secondLock = locks[Math.max(oldStripe, newStripe)];
                synchronized (firstLock) {
                    synchronized (secondLock) {
                        doMoveCross(id, oldChunk, newChunk, newX, newY, newZ, newPoint);
                    }
                }
            }
        }
        return oldPoint;
    }

    private void doMoveCross(int id, long oldChunk, long newChunk,
                             double newX, double newY, double newZ, Point newPoint) {
        final Bucket destBucket = bucket(newChunk);
        if (destBucket == null) byChunk.put(newChunk, Bucket.first(id, newX, newY, newZ));
        else byChunk.put(newChunk, destBucket.withAdded(id, newX, newY, newZ));
        byId.put(id, new Slot(newChunk, newPoint));
        final Bucket oldBucket = bucket(oldChunk);
        if (oldBucket != null) {
            final int idx = oldBucket.indexOf(id);
            if (idx != -1) {
                final Bucket newOldBucket = oldBucket.withRemovedAt(idx);
                if (newOldBucket == null) byChunk.remove(oldChunk);
                else byChunk.put(oldChunk, newOldBucket);
            }
        }
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
            for (var entry : byChunk.long2ObjectEntrySet()) {
                final long chunk = entry.getLongKey();
                final Object value = entry.getValue();
                if (value instanceof Bucket b) scanBucketForNearest(b, chunk, px, py, pz, state);
            }
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
                continue;
            }
            for (int dx = -r; dx <= r; dx++) {
                scanChunkForNearest(qcx + dx, qcz - r, px, py, pz, state);
                scanChunkForNearest(qcx + dx, qcz + r, px, py, pz, state);
            }
            for (int dz = -r + 1; dz <= r - 1; dz++) {
                scanChunkForNearest(qcx - r, qcz + dz, px, py, pz, state);
                scanChunkForNearest(qcx + r, qcz + dz, px, py, pz, state);
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
        final long chunk = chunkIndex(cx, cz);
        final Bucket b = bucket(chunk);
        if (b != null) scanBucketForNearest(b, chunk, px, py, pz, state);
    }

    private void scanBucketForNearest(Bucket b, long chunk, double px, double py, double pz, NearestState state) {
        final int n = b.size;
        final double[] xs = b.xs, ys = b.ys, zs = b.zs;
        final int[] ids = b.ids;
        double bestSq = state.bestSq;
        int bestId = state.bestId;
        for (int i = 0; i < n; i++) {
            final int id = ids[i];
            if (!belongsToChunk(id, chunk)) continue;
            final double ex = xs[i] - px, ey = ys[i] - py, ez = zs[i] - pz;
            final double d = ex * ex + ey * ey + ez * ez;
            if (d < bestSq) {
                bestSq = d;
                bestId = id;
            }
        }
        state.bestSq = bestSq;
        state.bestId = bestId;
    }

    private static final class NearestState {
        int bestId = -1;
        double bestSq = Double.POSITIVE_INFINITY;
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
            final long chunk = chunkIndex(minCX, minCZ);
            final Bucket b = bucket(chunk);
            if (b == null) return;
            forEachInBucketWithin(b, chunk, px, py, pz, rSq, consumer);
            return;
        }
        synchronized (crossChunkLock) {
            for (int cx = minCX; cx <= maxCX; cx++) {
                final double cxMin = cx * SECTION_SIZE_D, cxMax = cxMin + SECTION_SIZE_D;
                final double cdx = Math.max(0.0, Math.max(cxMin - px, px - cxMax));
                final double cdxSq = cdx * cdx;
                if (cdxSq >= rSq) continue;
                for (int cz = minCZ; cz <= maxCZ; cz++) {
                    final double czMin = cz * SECTION_SIZE_D, czMax = czMin + SECTION_SIZE_D;
                    final double cdz = Math.max(0.0, Math.max(czMin - pz, pz - czMax));
                    if (cdxSq + cdz * cdz >= rSq) continue;
                    final long chunk = chunkIndex(cx, cz);
                    final Bucket b = bucket(chunk);
                    if (b == null) continue;
                    forEachInBucketWithin(b, px, py, pz, rSq, consumer);
                }
            }
        }
    }

    private void forEachInBucketWithin(Bucket b, long chunk, double px, double py, double pz,
                                       double rSq, IntConsumer consumer) {
        final int n = b.size;
        final double[] xs = b.xs, ys = b.ys, zs = b.zs;
        final int[] ids = b.ids;
        for (int i = 0; i < n; i++) {
            final int id = ids[i];
            if (!belongsToChunk(id, chunk)) continue;
            final double dx = xs[i] - px, dy = ys[i] - py, dz = zs[i] - pz;
            if (dx * dx + dy * dy + dz * dz <= rSq) consumer.accept(id);
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
            final long chunk = chunkIndex(minCX, minCZ);
            final Bucket b = bucket(chunk);
            return b == null ? 0 : countInBucketWithin(b, chunk, px, py, pz, rSq);
        }
        int total = 0;
        synchronized (crossChunkLock) {
            for (int cx = minCX; cx <= maxCX; cx++) {
                final double cxMin = cx * SECTION_SIZE_D, cxMax = cxMin + SECTION_SIZE_D;
                final double cdx = Math.max(0.0, Math.max(cxMin - px, px - cxMax));
                final double cdxSq = cdx * cdx;
                if (cdxSq >= rSq) continue;
                for (int cz = minCZ; cz <= maxCZ; cz++) {
                    final double czMin = cz * SECTION_SIZE_D, czMax = czMin + SECTION_SIZE_D;
                    final double cdz = Math.max(0.0, Math.max(czMin - pz, pz - czMax));
                    if (cdxSq + cdz * cdz >= rSq) continue;
                    final long chunk = chunkIndex(cx, cz);
                    final Bucket b = bucket(chunk);
                    if (b == null) continue;
                    total += countInBucketWithin(b, px, py, pz, rSq);
                }
            }
        }
        return total;
    }

    private int countInBucketWithin(Bucket b, long chunk, double px, double py, double pz, double rSq) {
        final int n = b.size;
        final double[] xs = b.xs, ys = b.ys, zs = b.zs;
        final int[] ids = b.ids;
        int total = 0;
        for (int i = 0; i < n; i++) {
            final int id = ids[i];
            if (!belongsToChunk(id, chunk)) continue;
            final double dx = xs[i] - px, dy = ys[i] - py, dz = zs[i] - pz;
            if (dx * dx + dy * dy + dz * dz <= rSq) total++;
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
        final long chunk = chunkIndex(chunkX, chunkZ);
        final Bucket b = bucket(chunk);
        if (b == null) return;
        final int n = b.size;
        final int[] ids = b.ids;
        for (int i = 0; i < n; i++) {
            final int id = ids[i];
            if (belongsToChunk(id, chunk)) consumer.accept(id);
        }
    }

    @Override
    public @Unmodifiable IntCollection inChunk(int chunkX, int chunkZ) {
        return new ChunkView(this, chunkIndex(chunkX, chunkZ));
    }

    @Override
    public void forEachInChunkRange(Point point, int chunkRange, IntConsumer consumer) {
        final int centerX = point.chunkX();
        final int centerZ = point.chunkZ();
        final int minCX = centerX - chunkRange;
        final int maxCX = centerX + chunkRange;
        final int minCZ = centerZ - chunkRange;
        final int maxCZ = centerZ + chunkRange;
        synchronized (crossChunkLock) {
            for (int cx = minCX; cx <= maxCX; cx++) {
                for (int cz = minCZ; cz <= maxCZ; cz++) {
                    final long chunk = chunkIndex(cx, cz);
                    final Bucket b = bucket(chunk);
                    if (b == null) continue;
                    final int n = b.size;
                    final int[] ids = b.ids;
                    for (int i = 0; i < n; i++) {
                        consumer.accept(ids[i]);
                    }
                }
            }
        }
    }

    @Override
    public void forEachInChunkRangeDiffering(Point oldPoint, Point newPoint, int chunkRange,
                                             IntConsumer added, IntConsumer removed) {
        final int oldCX = oldPoint.chunkX(), oldCZ = oldPoint.chunkZ();
        final int newCX = newPoint.chunkX(), newCZ = newPoint.chunkZ();
        synchronized (crossChunkLock) {
            for (int cx = newCX - chunkRange; cx <= newCX + chunkRange; cx++) {
                for (int cz = newCZ - chunkRange; cz <= newCZ + chunkRange; cz++) {
                    if (Math.abs(cx - oldCX) <= chunkRange && Math.abs(cz - oldCZ) <= chunkRange) continue;
                    final long chunk = chunkIndex(cx, cz);
                    final Bucket b = bucket(chunk);
                    if (b == null) continue;
                    final int n = b.size;
                    final int[] ids = b.ids;
                    for (int i = 0; i < n; i++) {
                        added.accept(ids[i]);
                    }
                }
            }
            for (int cx = oldCX - chunkRange; cx <= oldCX + chunkRange; cx++) {
                for (int cz = oldCZ - chunkRange; cz <= oldCZ + chunkRange; cz++) {
                    if (Math.abs(cx - newCX) <= chunkRange && Math.abs(cz - newCZ) <= chunkRange) continue;
                    final long chunk = chunkIndex(cx, cz);
                    final Bucket b = bucket(chunk);
                    if (b == null) continue;
                    final int n = b.size;
                    final int[] ids = b.ids;
                    for (int i = 0; i < n; i++) {
                        removed.accept(ids[i]);
                    }
                }
            }
        }
    }

    record Slot(long chunk, Point point) {
    }

    record Bucket(int size, double[] xs, double[] ys, double[] zs, int[] ids) {
        static Bucket first(int id, double x, double y, double z) {
            return new Bucket(1,
                    new double[]{x}, new double[]{y}, new double[]{z},
                    new int[]{id});
        }

        int indexOf(int id) {
            final int[] ids = this.ids;
            final int n = size;
            for (int i = 0; i < n; i++) if (ids[i] == id) return i;
            return -1;
        }

        Bucket withAdded(int id, double x, double y, double z) {
            final int newSize = size + 1;
            final double[] nxs = Arrays.copyOf(xs, newSize);
            nxs[size] = x;
            final double[] nys = Arrays.copyOf(ys, newSize);
            nys[size] = y;
            final double[] nzs = Arrays.copyOf(zs, newSize);
            nzs[size] = z;
            final int[] nids = Arrays.copyOf(ids, newSize);
            nids[size] = id;
            return new Bucket(newSize, nxs, nys, nzs, nids);
        }

        @Nullable
        Bucket withRemovedAt(int idx) {
            final int newSize = size - 1;
            if (newSize == 0) return null;
            final double[] nxs = Arrays.copyOf(xs, newSize);
            final double[] nys = Arrays.copyOf(ys, newSize);
            final double[] nzs = Arrays.copyOf(zs, newSize);
            final int[] nids = Arrays.copyOf(ids, newSize);
            if (idx != newSize) {
                nxs[idx] = xs[newSize];
                nys[idx] = ys[newSize];
                nzs[idx] = zs[newSize];
                nids[idx] = ids[newSize];
            }
            return new Bucket(newSize, nxs, nys, nzs, nids);
        }

        Bucket withUpdated(int idx, double x, double y, double z) {
            final double[] nxs = Arrays.copyOf(xs, size);
            nxs[idx] = x;
            final double[] nys = Arrays.copyOf(ys, size);
            nys[idx] = y;
            final double[] nzs = Arrays.copyOf(zs, size);
            nzs[idx] = z;
            return new Bucket(size, nxs, nys, nzs, ids);
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
        private final PointIndexConcurrentImpl index;
        private final long chunkKey;

        ChunkView(PointIndexConcurrentImpl index, long chunkKey) {
            this.index = index;
            this.chunkKey = chunkKey;
        }

        @Override
        public IntIterator iterator() {
            final Bucket b = index.bucket(chunkKey);
            if (b == null) return IntIterators.EMPTY_ITERATOR;
            final int[] ids = b.ids;
            final int n = b.size;
            return new IntIterator() {
                int i = 0;
                int next;
                boolean hasNext;

                @Override
                public boolean hasNext() {
                    if (hasNext) return true;
                    while (i < n) {
                        final int id = ids[i++];
                        if (index.belongsToChunk(id, chunkKey)) {
                            next = id;
                            hasNext = true;
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public int nextInt() {
                    if (!hasNext()) throw new NoSuchElementException();
                    hasNext = false;
                    return next;
                }
            };
        }

        @Override
        public int size() {
            final Bucket b = index.bucket(chunkKey);
            if (b == null) return 0;
            int size = 0;
            final int[] ids = b.ids;
            for (int i = 0, n = b.size; i < n; i++) {
                if (index.belongsToChunk(ids[i], chunkKey)) size++;
            }
            return size;
        }

        @Override
        public boolean isEmpty() {
            final Bucket b = index.bucket(chunkKey);
            if (b == null) return true;
            final int[] ids = b.ids;
            for (int i = 0, n = b.size; i < n; i++) {
                if (index.belongsToChunk(ids[i], chunkKey)) return false;
            }
            return true;
        }
    }

}
