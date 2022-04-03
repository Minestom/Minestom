package net.minestom.server.event

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
class MultiNodeBenchmark {
    @Param("0", "1", "3", "10")
    var children = 0
    private var node: EventNode<Event>? = null

    internal inner class TestEvent : Event
    internal inner class TestEvent2 : Event

    @Setup
    fun setup() {
        node = EventNode.all("node")
        for (i in 0 until children) {
            val child = EventNode.all("child-$i")
            child.addListener(TestEvent::class.java) { e: TestEvent? -> }
            node!!.addChild(child)

            // Real-world code are very unlikely to use entirely empty nodes.
            // This ensures that the handle map is properly lazily initialized to prevent fast exits.
            child.addListener(TestEvent2::class.java) { e: TestEvent2? -> }
                .call(TestEvent2())
        }
    }

    @Benchmark
    fun call() {
        node!!.call(TestEvent())
    }
}