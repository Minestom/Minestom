package net.minestom.server.tag

import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound.set
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound.setString
import net.minestom.server.tag.TagHandler
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound
import net.minestom.server.tag.TagReadBenchmark
import java.util.concurrent.ConcurrentHashMap
import org.jglrxavpok.hephaistos.nbt.NBT
import org.openjdk.jmh.infra.Blackhole
import net.minestom.server.tag.TagWriteBenchmark
import java.util.function.IntFunction
import net.minestom.server.event.EventNode
import java.lang.Runnable
import net.minestom.server.timer.TaskSchedule
import java.util.concurrent.atomic.AtomicInteger
import net.minestom.server.instance.palette.Palette.EntrySupplier
import net.minestom.server.instance.palette.Palette.EntryConsumer
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(
    Mode.AverageTime
)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
class TagReadBenchmark {
    @Param("false", "true")
    var present = false
    var tagHandler: TagHandler? = null
    var secondTag: Tag<String>? = null
    var compound: MutableNBTCompound? = null
    @Setup
    fun setup() {
        // Tag benchmark
        tagHandler = TagHandler.newHandler()
        if (present) tagHandler!!.setTag(TAG, "value")
        secondTag = Tag.String("key")
        // NBT benchmark
        compound = MutableNBTCompound(ConcurrentHashMap())
        if (present) compound!!["key"] = NBT.String("value")
    }

    @Benchmark
    fun readConstantTag(blackhole: Blackhole) {
        blackhole.consume(tagHandler!!.getTag(TAG))
    }

    @Benchmark
    fun readDifferentTag(blackhole: Blackhole) {
        blackhole.consume(tagHandler!!.getTag(secondTag))
    }

    @Benchmark
    fun readNewTag(blackhole: Blackhole) {
        blackhole.consume(tagHandler!!.getTag(Tag.String("key")))
    }

    @Benchmark
    fun readConstantTagFromCompound(blackhole: Blackhole) {
        blackhole.consume(compound!!.getString("key"))
    }

    companion object {
        val TAG = Tag.String("key")
    }
}