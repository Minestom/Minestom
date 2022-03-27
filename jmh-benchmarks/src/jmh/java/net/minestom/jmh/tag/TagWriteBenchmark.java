package net.minestom.jmh.tag;

import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;
import org.openjdk.jmh.annotations.*;

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

    MutableNBTCompound compound;

    @Setup
    public void setup() {
        // Tag benchmark
        this.tagHandler = TagHandler.newHandler();
        tagHandler.setTag(TAG, "value");
        secondTag = Tag.String("key");
        // NBT benchmark
        this.compound = new MutableNBTCompound(new ConcurrentHashMap<>());
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
    public void writeConstantTagFromCompound() {
        compound.setString("key", "value");
    }
}
