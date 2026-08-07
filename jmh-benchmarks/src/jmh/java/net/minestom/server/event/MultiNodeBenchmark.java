package net.minestom.server.event;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class MultiNodeBenchmark {

    @Param({"0", "1", "3", "10"})
    public int children;

    private EventNode<Event> node;

    record TestEvent() implements Event {
    }

    record TestEvent2() implements Event {
    }

    @Setup
    public void setup() {
        node = EventNode.all("node");
        for (int i = 0; i < children; i++) {
            var child = EventNode.all("child-" + i);
            child.addListener(TestEvent.class, _ -> {
                // Empty
            });

            node.addChild(child);

            // Real-world code are very unlikely to use entirely empty nodes.
            // This ensures that the handle map is properly lazily initialized to prevent fast exits.
            child.addListener(TestEvent2.class, _ -> {
                // Empty
            }).call(new TestEvent2());
        }
    }

    @Benchmark
    public void call() {
        node.call(new TestEvent());
    }
}
