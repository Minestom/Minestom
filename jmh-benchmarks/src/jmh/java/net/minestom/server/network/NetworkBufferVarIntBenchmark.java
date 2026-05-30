package net.minestom.server.network;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 8, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Threads(4)
public class NetworkBufferVarIntBenchmark {

    private NetworkBuffer writeBuffer;
    private NetworkBuffer readBuffer;

    private static final int DATA_SIZE = 4096;
    private static final int MASK = DATA_SIZE - 1;

    private int[] mixedData;
    private int[] readPositions; // Offsets for reading different sized VarInts
    private int index;

    @Setup
    public void setup() {
        writeBuffer = NetworkBuffer.staticBuffer(256);
        readBuffer = NetworkBuffer.staticBuffer(DATA_SIZE * 5);

        Random random = new Random(67);
        mixedData = new int[DATA_SIZE];
        readPositions = new int[DATA_SIZE];

        for (int i = 0; i < DATA_SIZE; i++) {
            double r = random.nextDouble();
            int val;
            if (r < 0.5) val = random.nextInt(0, 128);
            else if (r < 0.8) val = random.nextInt(128, 16384);
            else val = random.nextInt();

            mixedData[i] = val;

            readPositions[i] = (int) readBuffer.writeIndex();
            readBuffer.write(NetworkBuffer.VAR_INT, val);
        }
    }

    @Benchmark
    public void writeVarint() {
        int val = mixedData[index++ & MASK];
        writeBuffer.writeAt(0, NetworkBuffer.VAR_INT, val);
    }

    @Benchmark
    public void readVarint(Blackhole bh) {
        int pos = readPositions[index++ & MASK];
        bh.consume(readBuffer.readAt(pos, NetworkBuffer.VAR_INT));
    }

    @TearDown
    public void teardown(Blackhole blackhole) {
        blackhole.consume(writeBuffer);
        blackhole.consume(readBuffer);
        blackhole.consume(mixedData);
        blackhole.consume(readPositions);
        blackhole.consume(index);
    }
}
