package net.minestom.server.network;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 8, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class NetworkBufferVarIntBenchmark {

    private NetworkBuffer buffer;

    private int one;
    private int two;
    private int three;
    private int four;
    private int five;

    @Setup
    public void setup() {
        buffer = NetworkBuffer.staticBuffer(256);

        one = ThreadLocalRandom.current().nextInt(0, 127 + 1);
        two = ThreadLocalRandom.current().nextInt(0, 16_383 + 1);
        three = ThreadLocalRandom.current().nextInt(0, 2_097_151 + 1);
        four = ThreadLocalRandom.current().nextInt(0, 268_435_455 + 1);
        five = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Benchmark
    public void varintZero() {
        buffer.clear();
        buffer.write(NetworkBuffer.VAR_INT, 0);
    }

    @Benchmark
    public void variantOne() {
        buffer.clear();
        buffer.write(NetworkBuffer.VAR_INT, one);
    }

    @Benchmark
    public void variantTwo() {
        buffer.clear();
        buffer.write(NetworkBuffer.VAR_INT, two);
    }

    @Benchmark
    public void variantThree() {
        buffer.clear();
        buffer.write(NetworkBuffer.VAR_INT, three);
    }

    @Benchmark
    public void variantFour() {
        buffer.clear();
        buffer.write(NetworkBuffer.VAR_INT, four);
    }

    @Benchmark
    public void variantFive() {
        buffer.clear();
        buffer.write(NetworkBuffer.VAR_INT, five);
    }

    @TearDown
    public void teardown(Blackhole blackhole) {
        blackhole.consume(buffer);
    }
}
