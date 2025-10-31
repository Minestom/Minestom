package net.minestom.server.network;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 8, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class NetworkBufferMakeArrayBenchmark {

    @Benchmark
    public void makeArray(Blackhole blackhole) {
        blackhole.consume(NetworkBuffer.makeArray(buffer -> {
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
        }));
    }
}
