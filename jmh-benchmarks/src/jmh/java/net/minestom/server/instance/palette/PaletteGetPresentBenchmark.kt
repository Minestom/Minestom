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
import java.util.*
import java.util.concurrent.TimeUnit

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(
    Mode.AverageTime
)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
class PaletteGetPresentBenchmark {
    @Param("0", "0.25", "0.5", "0.75", "1")
    var fullness = 0.0
    private var palette: Palette? = null
    @Setup
    fun setup() {
        palette = Palette.blocks()
        val random = Random(18932365)
        val dimension = palette.dimension()
        for (y in 0 until dimension) for (z in 0 until dimension) for (x in 0 until dimension) if (random.nextDouble() < fullness) palette.set(
            x,
            y,
            z,
            random.nextInt(1, 16)
        )
    }

    @Benchmark
    fun readAll(blackHole: Blackhole) {
        palette!!.getAll { x: Int, y: Int, z: Int, value: Int -> blackHole.consume(value) }
    }

    @Benchmark
    fun readAllPresent(blackHole: Blackhole) {
        palette!!.getAllPresent { x: Int, y: Int, z: Int, value: Int -> blackHole.consume(value) }
    }

    @Benchmark
    fun readAllPresentAlt(blackHole: Blackhole) {
        palette!!.getAll { x: Int, y: Int, z: Int, value: Int ->
            if (value != 0) {
                blackHole.consume(value)
            }
        }
    }
}