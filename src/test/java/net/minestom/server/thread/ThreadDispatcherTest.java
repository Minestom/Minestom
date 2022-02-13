package net.minestom.server.thread;

import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class ThreadDispatcherTest {

    @Test
    public void elementTick() {
        final AtomicInteger counter = new AtomicInteger();
        ThreadDispatcher<Object> dispatcher = ThreadDispatcher.singleThread();
        assertEquals(1, dispatcher.threads().size());
        assertThrows(Exception.class, () -> dispatcher.threads().add(new TickThread(1)));

        var partition = new Object();
        Tickable element = (time) -> counter.incrementAndGet();
        dispatcher.createPartition(partition);
        dispatcher.updateElement(element, partition);
        assertEquals(0, counter.get());

        dispatcher.updateAndAwait(System.currentTimeMillis());
        dispatcher.updateElement(element, partition); // Should be ignored
        dispatcher.createPartition(partition); // Ignored too
        assertEquals(1, counter.get());

        dispatcher.updateAndAwait(System.currentTimeMillis());
        assertEquals(2, counter.get());

        dispatcher.removeElement(element);
        dispatcher.updateAndAwait(System.currentTimeMillis());
        assertEquals(2, counter.get());

        dispatcher.shutdown();
    }

    @Test
    public void partitionTick() {
        // Partitions implementing Tickable should be ticked same as elements
        final AtomicInteger counter1 = new AtomicInteger();
        final AtomicInteger counter2 = new AtomicInteger();
        ThreadDispatcher<Tickable> dispatcher = ThreadDispatcher.singleThread();
        assertEquals(1, dispatcher.threads().size());

        Tickable partition = (time) -> counter1.incrementAndGet();
        Tickable element = (time) -> counter2.incrementAndGet();
        dispatcher.createPartition(partition);
        dispatcher.updateElement(element, partition);
        assertEquals(0, counter1.get());
        assertEquals(0, counter2.get());

        for (int i = 0; i < 100; i++) {
            dispatcher.updateAndAwait(System.currentTimeMillis());
            assertEquals(i + 1, counter1.get());
            assertEquals(i + 1, counter2.get());
        }

        dispatcher.deletePartition(partition);
        dispatcher.updateAndAwait(System.currentTimeMillis());
        assertEquals(100, counter1.get());
        assertEquals(100, counter2.get());

        dispatcher.shutdown();
    }

    @Test
    public void uniqueThread() {
        // Ensure that partitions are properly dispatched across threads
        final int threadCount = 10;
        ThreadDispatcher<Tickable> dispatcher = ThreadDispatcher.of(ThreadProvider.counter(), threadCount);
        assertEquals(threadCount, dispatcher.threads().size());

        final AtomicInteger counter = new AtomicInteger();
        Set<Thread> threads = new CopyOnWriteArraySet<>();
        Set<Tickable> partitions = IntStream.range(0, threadCount)
                .mapToObj(value -> (Tickable) (time) -> {
                    final Thread thread = Thread.currentThread();
                    assertInstanceOf(TickThread.class, thread);
                    assertEquals(1, ((TickThread) thread).entries().size());
                    assertTrue(threads.add(thread));
                    counter.getAndIncrement();
                })
                .collect(Collectors.toUnmodifiableSet());
        assertEquals(threadCount, partitions.size());

        partitions.forEach(dispatcher::createPartition);
        assertEquals(0, counter.get());

        dispatcher.updateAndAwait(System.currentTimeMillis());
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
        ThreadDispatcher<Updater> dispatcher = ThreadDispatcher.of(new ThreadProvider<>() {
            @Override
            public int findThread(@NotNull Updater partition) {
                return partition.getValue();
            }

            @Override
            public @NotNull RefreshType refreshType() {
                return RefreshType.ALWAYS;
            }
        }, threadCount);
        assertEquals(threadCount, dispatcher.threads().size());

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

        dispatcher.updateAndAwait(System.currentTimeMillis());

        dispatcher.refreshThreads();

        dispatcher.updateAndAwait(System.currentTimeMillis());

        assertEquals(threads2.size(), threads.size());
        assertNotEquals(threads, threads2, "Threads have not been updated at all");
        for (var entry : threads.entrySet()) {
            final Thread thread1 = entry.getValue();
            final Thread thread2 = threads2.get(entry.getKey());
            assertNotEquals(thread1, thread2);
        }

        dispatcher.shutdown();
    }
}
