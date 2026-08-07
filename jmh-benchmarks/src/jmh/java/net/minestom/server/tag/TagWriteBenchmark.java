package net.minestom.server.tag;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class TagWriteBenchmark {
    static final Tag<String> TAG = Tag.String("key");

    TagHandler tagHandler;
    Tag<String> secondTag;

    Map<String, String> map;
    Map<String, String> concurrentMap;

    @Setup
    public void setup() {
        // Tag benchmark
        this.tagHandler = TagHandler.newHandler();
        tagHandler.setTag(TAG, "value");
        secondTag = Tag.String("key");
        // Concurrent map benchmark
        map = new HashMap<>();
        map.put("key", "value");
        // Hash map benchmark
        concurrentMap = new ConcurrentHashMap<>();
        concurrentMap.put("key", "value");
    }

    @Benchmark
    public void writeConstantTag() {
        tagHandler.setTag(TAG, "value");
    }

    @Benchmark
    public void writeDifferentTag() {
        tagHandler.setTag(secondTag, "value");
    }

    @Benchmark
    public void writeNewTag() {
        tagHandler.setTag(Tag.String("key"), "value");
    }

    @Benchmark
    public void writeConcurrentMap() {
        concurrentMap.put("key", "value");
    }

    @Benchmark
    public void writeMap() {
        map.put("key", "value");
    }
}
