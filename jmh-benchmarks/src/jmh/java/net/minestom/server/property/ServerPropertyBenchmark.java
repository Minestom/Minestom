package net.minestom.server.property;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ServerPropertyBenchmark {

    private static final int CONSTANT_DIVISOR = 1234;
    private static final ServerProperty<Integer> DIVISOR = ServerPropertyImpl.create("minestom.benchmark.divisor", 1234, Integer::parseInt);

    private int dividend = 987654321;

    @Benchmark
    public int staticFinalConstant() {
        return dividend / CONSTANT_DIVISOR;
    }

    @Benchmark
    @Fork(jvmArgsAppend = "-Dminestom.benchmark.divisor.mutable=false")
    public int immutableProperty() {
        return dividend / DIVISOR.get();
    }

    @Benchmark
    @Fork(jvmArgsAppend = "-Dminestom.benchmark.divisor.mutable=true")
    public int mutableProperty() {
        return dividend / DIVISOR.get();
    }
}
