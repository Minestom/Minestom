package net.minestom.server.tag;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

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
public class TagReadBenchmark {
    static final Tag<String> TAG = Tag.String("key");

    @Param({"false", "true"})
    public boolean present;

    TagHandler tagHandler;
    Tag<String> secondTag;

    Map<String, String> map;
    Map<String, String> concurrentMap;

    @Setup
    public void setup() {
        // Tag benchmark
        this.tagHandler = TagHandler.newHandler();
        if (present) tagHandler.setTag(TAG, "value");
        secondTag = Tag.String("key");
        // Concurrent map benchmark
        map = new HashMap<>();
        if (present) map.put("key", "value");
        // Hash map benchmark
        concurrentMap = new ConcurrentHashMap<>();
        if (present) concurrentMap.put("key", "value");
    }

    @Benchmark
    public void readConstantTag(Blackhole blackhole) {
        blackhole.consume(tagHandler.getTag(TAG));
    }

    @Benchmark
    public void readDifferentTag(Blackhole blackhole) {
        blackhole.consume(tagHandler.getTag(secondTag));
    }

    @Benchmark
    public void readNewTag(Blackhole blackhole) {
        blackhole.consume(tagHandler.getTag(Tag.String("key")));
    }

    @Benchmark
    public void readConcurrentMap(Blackhole blackhole) {
        blackhole.consume(concurrentMap.get("key"));
    }

    @Benchmark
    public void readMap(Blackhole blackhole) {
        blackhole.consume(map.get("key"));
    }
}
