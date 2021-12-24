package net.minestom.jmh;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@Warmup(iterations = 2)
@Measurement(iterations = 2)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class SampleBenchmark {
    @Param("2")
    int min;

    @Param("10")
    int max;

    @Benchmark
    public long test() {
        var primeStream =
                LongStream.rangeClosed(min, max)
                        .mapToObj(BigInteger::valueOf)
                        .filter(i -> i.isProbablePrime(50));
        return primeStream.count();
    }
}
