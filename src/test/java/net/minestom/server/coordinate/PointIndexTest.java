package net.minestom.server.coordinate;

import it.unimi.dsi.fastutil.ints.IntCollection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class PointIndexTest {

    static Stream<Arguments> impls() {
        return Stream.of(
                Arguments.of("plain", (Supplier<PointIndex>) PointIndex::create),
                Arguments.of("concurrent", (Supplier<PointIndex>) PointIndex::createConcurrent)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void addGetContainsSize(String name, Supplier<PointIndex> factory) {
        PointIndex idx = factory.get();
        assertEquals(0, idx.size());
        assertNull(idx.get(1));
        assertFalse(idx.contains(1));

        Vec p = new Vec(0, 0, 0);
        idx.add(1, p);
        assertEquals(1, idx.size());
        assertEquals(p, idx.get(1));
        assertTrue(idx.contains(1));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void addDuplicateThrows(String name, Supplier<PointIndex> factory) {
        PointIndex idx = factory.get();
        idx.add(1, new Vec(0, 0, 0));
        assertThrows(IllegalStateException.class, () -> idx.add(1, new Vec(1, 0, 0)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void removeReturnsLastPointAndUpdatesSize(String name, Supplier<PointIndex> factory) {
        PointIndex idx = factory.get();
        Vec p = new Vec(7, 0, 3);
        idx.add(1, p);
        assertEquals(p, idx.remove(1));
        assertEquals(0, idx.size());
        assertNull(idx.remove(1));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void moveReturnsOldPointAndUpdatesPosition(String name, Supplier<PointIndex> factory) {
        PointIndex idx = factory.get();
        Vec p0 = new Vec(0, 0, 0);
        Vec p1 = new Vec(100, 0, 100);
        idx.add(1, p0);
        assertEquals(p0, idx.move(1, p1));
        assertEquals(p1, idx.get(1));

        Set<Integer> nearOrigin = new HashSet<>();
        idx.forEachWithin(new Vec(0, 0, 0), 10, nearOrigin::add);
        assertTrue(nearOrigin.isEmpty(), "id should have moved out of origin radius");

        Set<Integer> nearTarget = new HashSet<>();
        idx.forEachWithin(new Vec(100, 0, 100), 10, nearTarget::add);
        assertEquals(Set.of(1), nearTarget);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void moveUnknownReturnsNull(String name, Supplier<PointIndex> factory) {
        assertNull(factory.get().move(99, new Vec(0, 0, 0)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void nearestEmptyReturnsSentinel(String name, Supplier<PointIndex> factory) {
        assertEquals(-1, factory.get().nearest(new Vec(0, 0, 0)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void nearestPicksClosest(String name, Supplier<PointIndex> factory) {
        PointIndex idx = factory.get();
        idx.add(1, new Vec(100, 0, 0));
        idx.add(2, new Vec(5, 0, 0));
        idx.add(3, new Vec(50, 0, 0));
        assertEquals(2, idx.nearest(new Vec(0, 0, 0)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void withinReturnsCorrectSet(String name, Supplier<PointIndex> factory) {
        PointIndex idx = factory.get();
        idx.add(1, new Vec(0, 0, 0));
        idx.add(2, new Vec(5, 0, 0));
        idx.add(3, new Vec(20, 0, 0));
        idx.add(4, new Vec(0, 30, 0)); // Y component matters

        Set<Integer> found = new HashSet<>();
        idx.forEachWithin(new Vec(0, 0, 0), 10, found::add);
        assertEquals(Set.of(1, 2), found);

        assertEquals(2, idx.count(new Vec(0, 0, 0), 10));
        assertEquals(3, idx.count(new Vec(0, 0, 0), 25));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void withinCrossesChunkBoundaries(String name, Supplier<PointIndex> factory) {
        PointIndex idx = factory.get();
        idx.add(1, new Vec(-3, 0, -3));   // chunk (-1, -1)
        idx.add(2, new Vec(3, 0, 3));     // chunk (0, 0)
        idx.add(3, new Vec(20, 0, 20));   // chunk (1, 1)
        idx.add(4, new Vec(60, 0, 60));

        Set<Integer> found = new HashSet<>();
        idx.forEachWithin(new Vec(0, 0, 0), 30, found::add);
        assertEquals(Set.of(1, 2, 3), found);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void allReturnsLiveView(String name, Supplier<PointIndex> factory) {
        PointIndex idx = factory.get();
        idx.add(1, new Vec(0, 0, 0));
        idx.add(2, new Vec(0, 0, 0));
        assertEquals(2, idx.all().size());
        idx.remove(1);
        assertEquals(1, idx.all().size());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void forEachInChunkReturnsBucket(String name, Supplier<PointIndex> factory) {
        PointIndex idx = factory.get();
        idx.add(1, new Vec(2, 0, 2));    // chunk (0,0)
        idx.add(2, new Vec(10, 50, 10)); // chunk (0,0)
        idx.add(3, new Vec(20, 0, 0));   // chunk (1,0)

        Set<Integer> chunk00 = new HashSet<>();
        idx.forEachInChunk(0, 0, chunk00::add);
        assertEquals(Set.of(1, 2), chunk00);

        Set<Integer> chunk10 = new HashSet<>();
        idx.forEachInChunk(1, 0, chunk10::add);
        assertEquals(Set.of(3), chunk10);

        Set<Integer> empty = new HashSet<>();
        idx.forEachInChunk(99, 99, empty::add);
        assertTrue(empty.isEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void inChunkIsLiveView(String name, Supplier<PointIndex> factory) {
        PointIndex idx = factory.get();
        IntCollection view = idx.inChunk(0, 0);
        assertTrue(view.isEmpty());
        assertEquals(0, view.size());

        idx.add(1, new Vec(2, 0, 2));
        assertEquals(1, view.size());
        assertFalse(view.isEmpty());

        idx.add(2, new Vec(8, 0, 8));
        assertEquals(2, view.size());

        idx.remove(1);
        assertEquals(1, view.size());

        // Empty chunks return a live empty view too.
        IntCollection empty = idx.inChunk(99, 99);
        assertTrue(empty.isEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void forEachInChunkRangeIgnoresDistance(String name, Supplier<PointIndex> factory) {
        PointIndex idx = factory.get();
        idx.add(1, new Vec(2, 0, 2));     // chunk (0,0)
        idx.add(2, new Vec(20, 0, 20));   // chunk (1,1)
        idx.add(3, new Vec(40, 0, 40));   // chunk (2,2)
        idx.add(4, new Vec(100, 0, 100));

        Set<Integer> range1 = new HashSet<>();
        idx.forEachInChunkRange(new Vec(0, 0, 0), 1, range1::add);
        assertEquals(Set.of(1, 2), range1);

        Set<Integer> range2 = new HashSet<>();
        idx.forEachInChunkRange(new Vec(0, 0, 0), 2, range2::add);
        assertEquals(Set.of(1, 2, 3), range2);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void forEachInChunkRangeDifferingReportsSymmetricDifference(String name, Supplier<PointIndex> factory) {
        PointIndex idx = factory.get();
        idx.add(1, new Vec(0, 0, 0));    // chunk (0,0)
        idx.add(2, new Vec(100, 0, 0));  // chunk (6,0)
        idx.add(3, new Vec(20, 0, 20));  // chunk (1,1)
        idx.add(4, new Vec(80, 0, 0));   // chunk (5,0)

        Set<Integer> added = new HashSet<>();
        Set<Integer> removed = new HashSet<>();
        idx.forEachInChunkRangeDiffering(new Vec(0, 0, 0), new Vec(100, 0, 0), 1, added::add, removed::add);
        assertEquals(Set.of(2, 4), added);
        assertEquals(Set.of(1, 3), removed);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void forEachInChunkRangeDifferingSkipsOverlap(String name, Supplier<PointIndex> factory) {
        PointIndex idx = factory.get();
        idx.add(99, new Vec(20, 0, 0));
        idx.add(1, new Vec(0, 0, 0));   // chunk (0, 0)
        idx.add(2, new Vec(40, 0, 0));  // chunk (2, 0)

        Set<Integer> added = new HashSet<>();
        Set<Integer> removed = new HashSet<>();
        idx.forEachInChunkRangeDiffering(new Vec(0, 0, 0), new Vec(32, 0, 0), 1, added::add, removed::add);
        assertEquals(Set.of(2), added);
        assertEquals(Set.of(1), removed);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void singleChunkRadiusFastPath(String name, Supplier<PointIndex> factory) {
        PointIndex idx = factory.get();
        idx.add(1, new Vec(2, 0, 2));
        idx.add(2, new Vec(8, 0, 8));
        idx.add(3, new Vec(14, 0, 14));

        Set<Integer> found = new HashSet<>();
        idx.forEachWithin(new Vec(8, 0, 8), 3, found::add);
        assertEquals(Set.of(2), found);
        assertEquals(1, idx.count(new Vec(8, 0, 8), 3));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void swapRemoveKeepsBucketConsistent(String name, Supplier<PointIndex> factory) {
        PointIndex idx = factory.get();
        for (int i = 1; i <= 5; i++) idx.add(i, new Vec(i, 0, 0));

        idx.remove(2);
        assertNull(idx.get(2));
        assertEquals(4, idx.size());

        idx.move(5, new Vec(5, 0, 16));
        Set<Integer> found = new HashSet<>();
        idx.forEachInChunk(0, 1, found::add);
        assertEquals(Set.of(5), found);

        assertNotNull(idx.get(1));
        assertNotNull(idx.get(3));
        assertNotNull(idx.get(4));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("impls")
    void nearestAcrossManyChunks(String name, Supplier<PointIndex> factory) {
        PointIndex idx = factory.get();
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                idx.add((x + 10) * 100 + (z + 10), new Vec(x * 16 + 8, 0, z * 16 + 8));
            }
        }
        // 49 buckets; query at (50, 0, 50). Nearest is in chunk (3, 3) at (56, 0, 56).
        int nearest = idx.nearest(new Vec(50, 0, 50));
        assertNotEquals(-1, nearest);
        assertEquals(new Vec(56, 0, 56), idx.get(nearest));
    }

    @Test
    void concurrentAddMoveRemoveStaysConsistent() throws InterruptedException {
        final PointIndex idx = PointIndex.createConcurrent();
        final int threads = 8;
        final int idsPerThread = 200;
        final int movesPerId = 50;
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        final CountDownLatch start = new CountDownLatch(1);
        final AtomicInteger errors = new AtomicInteger();
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                final int threadId = t;
                futures.add(pool.submit(() -> {
                    try {
                        start.await();
                        final int base = threadId * idsPerThread;
                        for (int i = 0; i < idsPerThread; i++) {
                            idx.add(base + i + 1, new Vec(base + i, 64, base + i));
                        }
                        java.util.Random r = new java.util.Random(threadId);
                        for (int m = 0; m < movesPerId; m++) {
                            for (int i = 0; i < idsPerThread; i++) {
                                idx.move(base + i + 1, new Vec(
                                        r.nextDouble(-256, 256),
                                        r.nextDouble(0, 256),
                                        r.nextDouble(-256, 256)));
                            }
                        }
                        for (int i = 0; i < idsPerThread; i++) {
                            assertNotNull(idx.remove(base + i + 1), "remove " + (base + i + 1));
                        }
                    } catch (Throwable e) {
                        errors.incrementAndGet();
                        e.printStackTrace();
                    }
                }));
            }
            start.countDown();
            for (Future<?> f : futures) {
                try {
                    f.get(60, TimeUnit.SECONDS);
                } catch (Exception e) {
                    errors.incrementAndGet();
                    e.printStackTrace();
                }
            }
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        }
        assertEquals(0, errors.get(), "concurrent ops threw");
        assertEquals(0, idx.size(), "index leaked entries");
    }

    @Test
    void concurrentReadsDuringMutationAreNonNull() throws InterruptedException {
        final PointIndex idx = PointIndex.createConcurrent();
        for (int i = 0; i < 500; i++) {
            idx.add(i + 1, new Vec(i % 32, 64, i / 32));
        }
        final int writers = 2;
        final int readers = 4;
        final long durationNs = TimeUnit.MILLISECONDS.toNanos(500);
        final ExecutorService pool = Executors.newFixedThreadPool(writers + readers);
        final CountDownLatch start = new CountDownLatch(1);
        final AtomicInteger errors = new AtomicInteger();
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int w = 0; w < writers; w++) {
                final int seed = w;
                futures.add(pool.submit(() -> {
                    try {
                        start.await();
                        final long deadline = System.nanoTime() + durationNs;
                        java.util.Random r = new java.util.Random(seed);
                        while (System.nanoTime() < deadline) {
                            final int id = r.nextInt(500) + 1;
                            idx.move(id, new Vec(
                                    r.nextDouble(-256, 256),
                                    r.nextDouble(0, 256),
                                    r.nextDouble(-256, 256)));
                        }
                    } catch (Throwable e) {
                        errors.incrementAndGet();
                        e.printStackTrace();
                    }
                }));
            }
            for (int r = 0; r < readers; r++) {
                futures.add(pool.submit(() -> {
                    try {
                        start.await();
                        final long deadline = System.nanoTime() + durationNs;
                        while (System.nanoTime() < deadline) {
                            idx.forEachWithin(new Vec(0, 64, 0), 100, id -> {
                            });
                            idx.count(new Vec(0, 64, 0), 50);
                            idx.nearest(new Vec(0, 64, 0));
                        }
                    } catch (Throwable e) {
                        errors.incrementAndGet();
                        e.printStackTrace();
                    }
                }));
            }
            start.countDown();
            for (Future<?> f : futures) {
                try {
                    f.get(60, TimeUnit.SECONDS);
                } catch (Exception e) {
                    errors.incrementAndGet();
                    e.printStackTrace();
                }
            }
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        }
        assertEquals(0, errors.get(), "concurrent reads/writes threw");
    }

}
