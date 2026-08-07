package net.minestom.server.tag;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class TagWritePathBenchmark {
    @Param({"0", "1", "2", "3"})
    public int scope;

    TagHandler tagHandler;
    Tag<String> tag;

    @Setup
    public void setup() {
        this.tagHandler = TagHandler.newHandler();

        List<String> path = new ArrayList<>(scope);
        for (int i = 0; i < scope; i++) path.add("key" + i);
        this.tag = Tag.String("key").path(path.toArray(String[]::new));

        tagHandler.setTag(tag, "value");
    }

    @Benchmark
    public void write() {
        tagHandler.setTag(tag, "value");
    }
}
