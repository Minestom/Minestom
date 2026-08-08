package net.minestom.server.network;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 8, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class NetworkBufferStringBenchmark {

    private NetworkBuffer buffer;

    @Setup
    public void setup() {
        buffer = NetworkBuffer.resizableBuffer(8096);

        buffer.writeIndex(3);
        buffer.readIndex(3);

        buffer.write(NetworkBuffer.STRING, "hello i am bob, im quite a long string. It would be a shame to copy me twice");
    }

    @Benchmark
    public void read(Blackhole blackhole) {
        blackhole.consume(buffer.read(NetworkBuffer.STRING));
        buffer.readIndex(3);
    }

    @TearDown
    public void teardown(Blackhole blackhole) {
        blackhole.consume(buffer);
    }
}
