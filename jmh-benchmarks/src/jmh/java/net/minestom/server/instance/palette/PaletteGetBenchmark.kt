package net.minestom.server.instance.palette

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
class PaletteGetBenchmark {
    @Param("4", "16")
    var dimension = 0
    private var palette: Palette? = null
    @Setup
    fun setup() {
        palette = Palette.newPalette(dimension, 15, 4)
        val value = AtomicInteger()
        palette.setAll(EntrySupplier { x: Int, y: Int, z: Int -> value.getAndIncrement() })
    }

    @Benchmark
    fun read(blackHole: Blackhole) {
        val dimension = palette!!.dimension()
        for (x in 0 until dimension) {
            for (y in 0 until dimension) {
                for (z in 0 until dimension) {
                    blackHole.consume(palette!![x, y, z])
                }
            }
        }
    }

    @Benchmark
    fun readAll(blackHole: Blackhole) {
        palette!!.getAll { x: Int, y: Int, z: Int, value: Int -> blackHole.consume(value) }
    }
}