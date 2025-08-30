package net.minestom.server.network;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 8, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class NetworkBufferBenchmark {

    private NetworkBuffer buffer;

    @Setup()
    public void setup() {
        buffer = NetworkBuffer.resizableBuffer(8096);

        buffer.writeIndex(3);
        buffer.readIndex(3);

        buffer.write(NetworkBuffer.LONG, 54L);
        buffer.write(NetworkBuffer.INT, 54);
        buffer.write(NetworkBuffer.SHORT, (short) 54);
        buffer.write(NetworkBuffer.BYTE, (byte) 54);
        buffer.write(NetworkBuffer.BOOLEAN, true);
        buffer.write(NetworkBuffer.FLOAT, 54.0f);
        buffer.write(NetworkBuffer.DOUBLE, 54.0);
        buffer.write(NetworkBuffer.VAR_INT, 54);
        buffer.write(NetworkBuffer.VAR_LONG, 54L);
        buffer.write(NetworkBuffer.STRING, "4");
    }

    @Benchmark
    public void create(Blackhole blackhole) {
        blackhole.consume(NetworkBuffer.resizableBuffer(8096));
    }

    @Benchmark
    public void read(Blackhole blackhole) {
        blackhole.consume(buffer.read(NetworkBuffer.LONG));
        blackhole.consume(buffer.read(NetworkBuffer.INT));
        blackhole.consume(buffer.read(NetworkBuffer.SHORT));
        blackhole.consume(buffer.read(NetworkBuffer.BYTE));
        blackhole.consume(buffer.read(NetworkBuffer.BOOLEAN));
        blackhole.consume(buffer.read(NetworkBuffer.FLOAT));
        blackhole.consume(buffer.read(NetworkBuffer.DOUBLE));
        blackhole.consume(buffer.read(NetworkBuffer.VAR_INT));
        blackhole.consume(buffer.read(NetworkBuffer.VAR_LONG));
        blackhole.consume(buffer.read(NetworkBuffer.STRING));
        buffer.readIndex(3);
    }

    @TearDown
    public void teardown(Blackhole blackhole) {
        blackhole.consume(buffer);
    }
}
