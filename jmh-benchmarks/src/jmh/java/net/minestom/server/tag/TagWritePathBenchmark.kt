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
import java.util.ArrayList
import java.util.concurrent.TimeUnit

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(
    Mode.AverageTime
)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
class TagWritePathBenchmark {
    @Param("0", "1", "2", "3")
    var scope = 0
    var tagHandler: TagHandler? = null
    var tag: Tag<String>? = null
    @Setup
    fun setup() {
        tagHandler = TagHandler.newHandler()
        val path: MutableList<String> = ArrayList(scope)
        for (i in 0 until scope) path.add("key$i")
        tag = Tag.String("key").path(*path.toArray { _Dummy_.__Array__() })
        tagHandler!!.setTag(tag, "value")
    }

    @Benchmark
    fun write() {
        tagHandler!!.setTag(tag, "value")
    }
}