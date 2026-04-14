package net.minestom.server.network;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.foreign.Arena;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class NetworkBufferAllocationBenchmark {

    @Param({"8", "32", "64", "128", "256", "512", "1024", "2048", "4096", "8192"})
    public long length;

    @Benchmark
    @Fork(value = 3, jvmArgsAppend = "-Dminestom.attempt-native-allocation=false")
    public void confinedBuiltin(Blackhole blackhole) {
        try (var arena = Arena.ofConfined()) {
            var allocator = NetworkBufferAllocator.staticAllocator().arena(arena);
            var allocation = allocator.allocate(length);
            blackhole.consume(allocation);
        }
    }

    @Benchmark
    @Fork(value = 3, jvmArgsAppend = "-Dminestom.force-native-allocation=true")
    public void confinedNative(Blackhole blackhole) {
        try (var arena = Arena.ofConfined()) {
            var allocator = NetworkBufferAllocator.staticAllocator().arena(arena);
            var allocation = allocator.allocate(length);
            blackhole.consume(allocation);
        }
    }
}