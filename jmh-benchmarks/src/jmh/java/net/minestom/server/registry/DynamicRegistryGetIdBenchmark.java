package net.minestom.server.registry;

import net.minestom.server.MinecraftServer;
import net.minestom.server.world.biome.Biome;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class DynamicRegistryGetIdBenchmark {
    private DynamicRegistry<Biome> registry;
    private final RegistryKey<Biome> biome = Biome.PALE_GARDEN;

    @Setup
    public void setup() {
        registry = Biome.createDefaultRegistry();
    }

    @Benchmark
    public int getId() {
        return registry.getId(biome);
    }
}
