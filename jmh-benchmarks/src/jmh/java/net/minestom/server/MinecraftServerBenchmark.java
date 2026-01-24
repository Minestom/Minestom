package net.minestom.server;


import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class MinecraftServerBenchmark {
    static {
        MinecraftServer.init();
    }

    @Benchmark
    @Fork(3)
    public ServerProcess processImmutable() {
        return MinecraftServer.process();
    }

    @Benchmark
    @Fork(value = 3, jvmArgsAppend = "-Dminestom.allow-multiple-initializations=true")
    public ServerProcess processMutable() {
        return MinecraftServer.process();
    }
}
