package net.minestom.server.network;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.foreign.Arena;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 8, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class NetworkBufferCreateBenchmark {

    @Benchmark
    public void createConfined(Blackhole blackhole) {
        try (var arena = Arena.ofConfined()) {
            var settings = NetworkBuffer.Factory.staticFactory().arena(arena);
            var allocation = settings.allocate(256);
            blackhole.consume(allocation);
        }
    }

    @Benchmark
    public void createShared(Blackhole blackhole) {
        try (var arena = Arena.ofShared()) {
            var settings = NetworkBuffer.Factory.staticFactory().arena(arena);
            var allocation = settings.allocate(256);
            blackhole.consume(allocation);
        }
    }
}