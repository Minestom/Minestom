package net.minestom.jmh.event;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.ListenerHandle;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class SingleNodeBenchmark {

    @Param({"0", "1", "2", "3", "5", "10"})
    public int listenerCount;

    private EventNode<Event> node;
    private ListenerHandle<TestEvent> handle;

    record TestEvent() implements Event {
    }

    @Setup
    public void setup() {
        node = EventNode.all("node");
        for (int i = 0; i < listenerCount; i++) {
            node.addListener(TestEvent.class, testEvent -> {
                // Empty
            });
        }
        this.handle = node.getHandle(TestEvent.class);
    }

    @Benchmark
    public void call() {
        node.call(new TestEvent());
    }

    @Benchmark
    public void handleCall() {
        handle.call(new TestEvent());
    }
}
