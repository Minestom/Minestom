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
class PaletteSetBenchmark {
    @Param("4", "16")
    var dimension = 0
    private var palette: Palette? = null
    @Setup
    fun setup() {
        palette = Palette.newPalette(dimension, 15, 4)
    }

    @Benchmark
    fun incrWrite() {
        var value = 0
        val dimension = palette!!.dimension()
        for (x in 0 until dimension) {
            for (y in 0 until dimension) {
                for (z in 0 until dimension) {
                    palette!![x, y, z] = value++
                }
            }
        }
    }

    @Benchmark
    fun incrWriteAll() {
        val value = AtomicInteger(0)
        palette!!.setAll { x: Int, y: Int, z: Int ->
            val v = value.getPlain()
            value.plain = v + 1
            v
        }
    }

    @Benchmark
    fun constantWrite() {
        val dimension = palette!!.dimension()
        for (x in 0 until dimension) {
            for (y in 0 until dimension) {
                for (z in 0 until dimension) {
                    palette!![x, y, z] = 5
                }
            }
        }
    }

    @Benchmark
    fun constantWriteAll() {
        palette!!.setAll { x: Int, y: Int, z: Int -> 5 }
    }

    @Benchmark
    fun fill() {
        palette!!.fill(5)
    }
}