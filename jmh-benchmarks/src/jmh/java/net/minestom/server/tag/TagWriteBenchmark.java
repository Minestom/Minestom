package net.minestom.server.tag;

import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
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

    MutableNBTCompound concurrentCompound;
    MutableNBTCompound compound;

    @Setup
    public void setup() {
        // Tag benchmark
        this.tagHandler = TagHandler.newHandler();
        tagHandler.setTag(TAG, "value");
        secondTag = Tag.String("key");
        // Concurrent map benchmark
        this.concurrentCompound = new MutableNBTCompound(new ConcurrentHashMap<>());
        concurrentCompound.set("key", NBT.String("value"));
        // Hash map benchmark
        this.compound = new MutableNBTCompound(new HashMap<>());
        compound.set("key", NBT.String("value"));
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
    public void writeConcurrentCompound() {
        concurrentCompound.setString("key", "value");
    }

    @Benchmark
    public void writeCompound() {
        compound.setString("key", "value");
    }
}
