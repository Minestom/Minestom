package thread;

import net.minestom.server.Tickable;
import net.minestom.server.thread.ThreadDispatcher;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThreadDispatcherTest {

    @Test
    public void elementTick() {
        record Partition() {
        }
        final AtomicInteger counter = new AtomicInteger();
        ThreadDispatcher<Partition> dispatcher = ThreadDispatcher.singleThread();
        assertEquals(1, dispatcher.threads().size());

        var partition = new Partition();
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
    public void partitionTick(){
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
    }
}
