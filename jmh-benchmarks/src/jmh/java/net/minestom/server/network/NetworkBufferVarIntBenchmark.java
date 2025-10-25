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
@OutputTimeUnit(TimeUnit.NANOSECONDS)
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

        one = ThreadLocalRandom.current().nextInt(1, 127 + 1);
        two = ThreadLocalRandom.current().nextInt(127 + 1, 16_383 + 1);
        three = ThreadLocalRandom.current().nextInt(16_383 + 1, 2_097_151 + 1);
        four = ThreadLocalRandom.current().nextInt(2_097_151 + 1, 268_435_455 + 1);
        five = ThreadLocalRandom.current().nextInt(268_435_455 + 1, Integer.MAX_VALUE);
    }

    @Benchmark
    public void varintZero() {
        buffer.clear();
        buffer.write(NetworkBuffer.VAR_INT, 0);
    }

    @Benchmark
    public void varintOne() {
        buffer.clear();
        buffer.write(NetworkBuffer.VAR_INT, one);
    }

    @Benchmark
    public void varintTwo() {
        buffer.clear();
        buffer.write(NetworkBuffer.VAR_INT, two);
    }

    @Benchmark
    public void varintThree() {
        buffer.clear();
        buffer.write(NetworkBuffer.VAR_INT, three);
    }

    @Benchmark
    public void varintFour() {
        buffer.clear();
        buffer.write(NetworkBuffer.VAR_INT, four);
    }

    @Benchmark
    public void varintFive() {
        buffer.clear();
        buffer.write(NetworkBuffer.VAR_INT, five);
    }

    @TearDown
    public void teardown(Blackhole blackhole) {
        blackhole.consume(buffer);
    }
}
