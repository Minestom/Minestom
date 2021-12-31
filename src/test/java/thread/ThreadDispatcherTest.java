package thread;

import net.minestom.server.Tickable;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.thread.ThreadProvider;
import net.minestom.server.thread.TickThread;
import org.junit.jupiter.api.Test;

import java.util.Set;
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

        var partition = new Object();
        Tickable element = (time) -> counter.incrementAndGet();
        dispatcher.createPartition(partition);
        dispatcher.updateElement(element, partition);
        assertEquals(0, counter.get());

        dispatcher.updateAndAwait(System.currentTimeMillis());
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

        dispatcher.updateAndAwait(System.currentTimeMillis());
        assertEquals(1, counter1.get());
        assertEquals(1, counter2.get());

        dispatcher.updateAndAwait(System.currentTimeMillis());
        assertEquals(2, counter1.get());
        assertEquals(2, counter2.get());

        dispatcher.deletePartition(partition);
        dispatcher.updateAndAwait(System.currentTimeMillis());
        assertEquals(2, counter1.get());
        assertEquals(2, counter2.get());

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
        assertEquals(10, counter.get());

        dispatcher.shutdown();
    }
}
