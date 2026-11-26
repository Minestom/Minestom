package net.minestom.server.thread;

import net.minestom.server.Tickable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ThreadDispatcherTest {
    record World() {
    }

    static abstract class Element implements Tickable, AcquirableSource<Element> {
        final Acquirable<Element> acquirable = Acquirable.unassigned(this);

        @Override
        public Acquirable<? extends Element> acquirable() {
            return acquirable;
        }
    }

    @Test
    public void basic() {
        final class Counter implements Tickable {
            int value;

            @Override
            public void tick(long time) {
                value++;
            }
        }
        World world = new World();
        Counter element = new Counter();

        ThreadDispatcher<World, Counter> dispatcher = ThreadDispatcher.singleThread();
        dispatcher.createPartition(world);
        dispatcher.updateElement(element, world);
        dispatcher.start();

        assertEquals(0, element.value);
        dispatcher.updateAndAwait(0);
        assertEquals(1, element.value);

        dispatcher.shutdown();
    }

    @Test
    public void basicAcquirable() {
        World world = new World();
        Element element = new Element() {
            @Override
            public void tick(long time) {
            }
        };

        ThreadDispatcher<World, Element> dispatcher = ThreadDispatcher.singleThread();
        dispatcher.createPartition(world);
        dispatcher.updateElement(element, world);
        dispatcher.start();

        assertNull(element.acquirable().assignedThread());
        dispatcher.updateAndAwait(0);
        assertNotNull(element.acquirable().assignedThread());

        dispatcher.shutdown();
    }

    @Test
    public void elementTick() {
        final AtomicInteger counter = new AtomicInteger();
        ThreadDispatcher<World, Tickable> dispatcher = ThreadDispatcher.singleThread();
        dispatcher.start();
        assertEquals(1, dispatcher.threads().size());
        assertThrows(Exception.class, () -> dispatcher.threads().add(new TickThread(1)));

        var partition = new World();
        Tickable element = (_) -> counter.incrementAndGet();
        dispatcher.createPartition(partition);
        dispatcher.updateElement(element, partition);
        assertEquals(0, counter.get());

        dispatcher.updateAndAwait(System.nanoTime());
        dispatcher.updateElement(element, partition); // Should be ignored
        dispatcher.createPartition(partition); // Ignored too
        assertEquals(1, counter.get());

        dispatcher.updateAndAwait(System.nanoTime());
        assertEquals(2, counter.get());

        dispatcher.removeElement(element);
        dispatcher.updateAndAwait(System.nanoTime());
        assertEquals(2, counter.get());

        dispatcher.shutdown();
    }

    @Test
    public void elementTickLoop() {
        final AtomicInteger counter = new AtomicInteger();
        ThreadDispatcher<World, Tickable> dispatcher = ThreadDispatcher.singleThread();
        dispatcher.start();

        var partition = new World();
        Tickable element = (_) -> counter.incrementAndGet();
        dispatcher.createPartition(partition);
        dispatcher.updateElement(element, partition);
        assertEquals(0, counter.get());

        for (int i = 0; i < 100; i++) {
            dispatcher.updateAndAwait(System.nanoTime());
            assertEquals(i + 1, counter.get());
        }

        dispatcher.shutdown();
    }

    @Test
    public void elementTickLoopAsync() {
        final AtomicInteger counter = new AtomicInteger();
        ThreadDispatcher<World, Tickable> dispatcher = ThreadDispatcher.singleThread();
        dispatcher.start();

        var partition = new World();
        Tickable element = (_) -> counter.incrementAndGet();
        dispatcher.createPartition(partition);
        dispatcher.updateElement(element, partition);
        assertEquals(0, counter.get());

        final int count = 100;
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            Thread.startVirtualThread(() -> {
                dispatcher.updateAndAwait(System.nanoTime());
                latch.countDown();
            });
        }
        try {
            latch.await();
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            fail("Latch was interrupted");
        }
        assertEquals(count, counter.get());

        dispatcher.shutdown();
    }

    @Test
    public void partitionTick() {
        // Partitions implementing Tickable should be ticked same as elements
        final AtomicInteger counter1 = new AtomicInteger();
        final AtomicInteger counter2 = new AtomicInteger();
        ThreadDispatcher<Tickable, Tickable> dispatcher = ThreadDispatcher.singleThread();
        dispatcher.start();
        assertEquals(1, dispatcher.threads().size());

        Tickable partition = (_) -> counter1.incrementAndGet();
        Tickable element = (_) -> counter2.incrementAndGet();
        dispatcher.createPartition(partition);
        dispatcher.updateElement(element, partition);
        assertEquals(0, counter1.get());
        assertEquals(0, counter2.get());

        for (int i = 0; i < 100; i++) {
            dispatcher.updateAndAwait(System.nanoTime());
            assertEquals(i + 1, counter1.get());
            assertEquals(i + 1, counter2.get());
        }

        dispatcher.deletePartition(partition);
        dispatcher.updateAndAwait(System.nanoTime());
        assertEquals(100, counter1.get());
        assertEquals(100, counter2.get());

        dispatcher.shutdown();
    }

    @Test
    public void uniqueThread() {
        // Ensure that partitions are properly dispatched across threads
        final int threadCount = 10;
        ThreadDispatcher<Tickable, Tickable> dispatcher = ThreadDispatcher.dispatcher(ThreadProvider.counter(), threadCount);
        assertEquals(threadCount, dispatcher.threads().size());
        dispatcher.start();

        final AtomicInteger counter = new AtomicInteger();
        Set<Thread> threads = new CopyOnWriteArraySet<>();
        Set<Tickable> partitions = IntStream.range(0, threadCount)
                .mapToObj(_ -> (Tickable) (_) -> {
                    final Thread thread = Thread.currentThread();
                    assertInstanceOf(TickThread.class, thread);
                    assertEquals(1, ((TickThread) thread).entries.size());
                    assertTrue(threads.add(thread));
                    counter.getAndIncrement();
                })
                .collect(Collectors.toUnmodifiableSet());
        assertEquals(threadCount, partitions.size());

        partitions.forEach(dispatcher::createPartition);
        assertEquals(0, counter.get());

        dispatcher.updateAndAwait(System.nanoTime());
        assertEquals(threadCount, counter.get());

        dispatcher.shutdown();
    }

    @Test
    public void threadUpdate() {
        // Ensure that partitions threads are properly updated every tick
        // when RefreshType.ALWAYS is used
        interface Updater extends Tickable {
            int getValue();
        }

        final int threadCount = 10;
        ThreadDispatcher<Updater, Tickable> dispatcher = ThreadDispatcher.dispatcher(new ThreadProvider<>() {
            @Override
            public int findThread(Updater partition) {
                return partition.getValue();
            }

            @Override
            public RefreshType refreshType() {
                return RefreshType.ALWAYS;
            }
        }, threadCount);
        assertEquals(threadCount, dispatcher.threads().size());
        dispatcher.start();

        Map<Updater, Thread> threads = new ConcurrentHashMap<>();
        Map<Updater, Thread> threads2 = new ConcurrentHashMap<>();
        Set<Updater> partitions = IntStream.range(0, threadCount)
                .mapToObj(value -> new Updater() {
                    private int v = value;

                    @Override
                    public int getValue() {
                        return v;
                    }

                    @Override
                    public void tick(long time) {
                        final Thread currentThread = Thread.currentThread();
                        assertInstanceOf(TickThread.class, currentThread);
                        if (threads.putIfAbsent(this, currentThread) == null) {
                            this.v = value + 1;
                        } else {
                            assertEquals(value + 1, v);
                            threads2.putIfAbsent(this, currentThread);
                        }
                    }
                }).collect(Collectors.toUnmodifiableSet());
        assertEquals(threadCount, partitions.size());

        partitions.forEach(dispatcher::createPartition);

        dispatcher.updateAndAwait(System.nanoTime());

        dispatcher.refreshThreads();

        dispatcher.updateAndAwait(System.nanoTime());

        assertEquals(threads2.size(), threads.size());
        assertNotEquals(threads, threads2, "Threads have not been updated at all");
        for (var entry : threads.entrySet()) {
            final Thread thread1 = entry.getValue();
            final Thread thread2 = threads2.get(entry.getKey());
            assertNotEquals(thread1, thread2);
        }

        dispatcher.shutdown();
    }

    // Identity-based equality, a record would hash the mutable tick log
    static final class OrderedPartition implements Tickable {
        private final int id;
        private final List<Integer> ticks;

        OrderedPartition(int id, List<Integer> ticks) {
            this.id = id;
            this.ticks = ticks;
        }

        int id() {
            return id;
        }

        @Override
        public void tick(long time) {
            ticks.add(id);
        }
    }

    @Test
    public void partitionRemovalKeepsOrder() {
        List<Integer> ticks = Collections.synchronizedList(new ArrayList<>());
        ThreadDispatcher<Tickable, Tickable> dispatcher = ThreadDispatcher.singleThread();
        dispatcher.start();
        final TickThread thread = dispatcher.threads().getFirst();

        final int count = 100;
        List<OrderedPartition> partitions = IntStream.range(0, count)
                .mapToObj(id -> new OrderedPartition(id, ticks)).toList();
        partitions.forEach(dispatcher::createPartition);
        dispatcher.updateAndAwait(System.nanoTime());
        assertEquals(IntStream.range(0, count).boxed().toList(), ticks);
        assertEquals(count, thread.entryCount());

        // Remove a few partitions, without triggering a compaction
        List<Integer> expected = new ArrayList<>(IntStream.range(0, count).boxed().toList());
        for (int id : List.of(0, 42, 57, 99)) {
            dispatcher.deletePartition(partitions.get(id));
            expected.removeIf(value -> value == id);
        }
        ticks.clear();
        dispatcher.updateAndAwait(System.nanoTime());
        assertEquals(expected, ticks);
        assertEquals(expected.size(), thread.entryCount());

        // Remove most partitions, triggering compactions
        expected.stream().filter(id -> id < 90).forEach(id -> dispatcher.deletePartition(partitions.get(id)));
        expected.removeIf(id -> id < 90);
        // Reloaded partitions are ticked last
        dispatcher.createPartition(partitions.get(0));
        dispatcher.createPartition(partitions.get(42));
        expected.add(0);
        expected.add(42);
        ticks.clear();
        dispatcher.updateAndAwait(System.nanoTime());
        assertEquals(expected, ticks);
        assertEquals(expected.size(), thread.entryCount());
        assertTrue(thread.entries.size() < count, "Entries have not been compacted");

        // Removing every partition leaves nothing to tick
        expected.forEach(id -> dispatcher.deletePartition(partitions.get(id)));
        ticks.clear();
        dispatcher.updateAndAwait(System.nanoTime());
        assertEquals(List.of(), ticks);
        assertEquals(0, thread.entryCount());

        dispatcher.shutdown();
    }

    @Test
    public void partitionRemovalWithRefresh() {
        // Unloaded partitions must be dropped from the thread refresh queue
        final int threadCount = 4;
        Map<Integer, AtomicInteger> lookups = new ConcurrentHashMap<>();
        AtomicInteger shift = new AtomicInteger();
        ThreadDispatcher<OrderedPartition, Tickable> dispatcher = ThreadDispatcher.dispatcher(new ThreadProvider<>() {
            @Override
            public int findThread(OrderedPartition partition) {
                lookups.computeIfAbsent(partition.id(), _ -> new AtomicInteger()).incrementAndGet();
                return partition.id() + shift.get();
            }

            @Override
            public RefreshType refreshType() {
                return RefreshType.ALWAYS;
            }
        }, threadCount);
        dispatcher.start();

        final int count = 40;
        List<Integer> ticks = Collections.synchronizedList(new ArrayList<>());
        List<OrderedPartition> partitions = IntStream.range(0, count)
                .mapToObj(id -> new OrderedPartition(id, ticks)).toList();
        partitions.forEach(dispatcher::createPartition);
        dispatcher.updateAndAwait(System.nanoTime());
        assertEquals(count, ticks.size());

        // Unload most partitions, then reload one of them before the queue is refreshed
        for (int id = 0; id < 30; id++) dispatcher.deletePartition(partitions.get(id));
        dispatcher.createPartition(partitions.get(5));
        dispatcher.updateAndAwait(System.nanoTime());

        List<Integer> alive = new ArrayList<>(List.of(5));
        for (int id = 30; id < count; id++) alive.add(id);
        lookups.clear();
        shift.set(1);
        dispatcher.refreshThreads();
        // Each loaded partition is refreshed exactly once, unloaded ones are not refreshed at all
        assertEquals(Set.copyOf(alive), lookups.keySet());
        lookups.values().forEach(lookup -> assertEquals(1, lookup.get()));

        ticks.clear();
        dispatcher.updateAndAwait(System.nanoTime());
        assertEquals(Set.copyOf(alive), Set.copyOf(ticks));
        assertEquals(alive.size(), ticks.size());
        int entryCount = 0;
        for (TickThread thread : dispatcher.threads()) entryCount += thread.entryCount();
        assertEquals(alive.size(), entryCount);

        dispatcher.shutdown();
    }
}
