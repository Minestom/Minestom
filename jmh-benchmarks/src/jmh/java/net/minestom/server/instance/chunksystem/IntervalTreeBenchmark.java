package net.minestom.server.instance.chunksystem;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class IntervalTreeBenchmark {
    private IntervalTree<Integer> tree;

    @Setup
    public void setup() {
        tree = new IntervalTree<>();
        var random = new Random(105976);
        var len = 100_000;
        for (var i = 0; i < len; i++) {
            var num = random.nextInt(len);
            tree.insertOrGet(num, num + 50, () -> num);
        }
    }

    @Benchmark
    public void search(Blackhole blackhole) {
        blackhole.consume(tree.searchNodes(5834));
    }
}
