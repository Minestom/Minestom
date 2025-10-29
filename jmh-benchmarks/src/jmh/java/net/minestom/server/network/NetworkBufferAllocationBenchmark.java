package net.minestom.server.network;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.foreign.Arena;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class NetworkBufferAllocationBenchmark {

    @Param({"128", "256", "512", "1024", "2048", "4096", "8192"})
    public long length;

    @Benchmark
    public void createConfined(Blackhole blackhole) {
        try (var arena = Arena.ofConfined()) {
            var settings = NetworkBufferFactory.staticFactory().arena(arena);
            var allocation = settings.allocate(length);
            blackhole.consume(allocation);
        }
    }
}