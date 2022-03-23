package net.minestom.jmh.tag;

import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

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

    MutableNBTCompound compound;

    @Setup
    public void setup() {
        // Tag benchmark
        this.tagHandler = TagHandler.newHandler();
        if (present) tagHandler.setTag(TAG, "value");
        secondTag = Tag.String("key");
        // NBT benchmark
        this.compound = new MutableNBTCompound(new ConcurrentHashMap<>());
        if (present) compound.set("key", NBT.String("value"));
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
    public void readConstantTagFromCompound(Blackhole blackhole) {
        blackhole.consume(compound.getString("key"));
    }
}
