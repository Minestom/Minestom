package net.minestom.server.network;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 8, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class NetworkBufferRawArrayBenchmark {

    @Param({"16", "1024"})
    public int count;

    private NetworkBuffer writeBuffer, readBuffer;

    private byte[] bytes;
    private short[] shorts;
    private int[] ints;
    private long[] longs;
    private float[] floats;
    private double[] doubles;

    private NetworkBuffer.Type<byte[]> bytesType;
    private NetworkBuffer.Type<short[]> shortsType;
    private NetworkBuffer.Type<int[]> intsType;
    private NetworkBuffer.Type<long[]> longsType;
    private NetworkBuffer.Type<float[]> floatsType;
    private NetworkBuffer.Type<double[]> doublesType;

    @Setup
    public void setup() {
        bytes = new byte[count];
        shorts = new short[count];
        ints = new int[count];
        longs = new long[count];
        floats = new float[count];
        doubles = new double[count];
        for (int i = 0; i < count; i++) {
            bytes[i] = (byte) i;
            shorts[i] = (short) i;
            ints[i] = i;
            longs[i] = i;
            floats[i] = i;
            doubles[i] = i;
        }

        bytesType = NetworkBuffer.FixedRawBytes(count);
        shortsType = NetworkBuffer.FixedRawShorts(count);
        intsType = NetworkBuffer.FixedRawInts(count);
        longsType = NetworkBuffer.FixedRawLongs(count);
        floatsType = NetworkBuffer.FixedRawFloats(count);
        doublesType = NetworkBuffer.FixedRawDoubles(count);

        final long capacity = (long) count * Double.BYTES;
        writeBuffer = NetworkBuffer.staticBuffer(capacity);
        readBuffer = NetworkBuffer.staticBuffer(capacity);
        readBuffer.write(NetworkBuffer.RAW_DOUBLES, doubles);
    }

    @Benchmark
    public NetworkBuffer writeBytes() {
        final NetworkBuffer buffer = this.writeBuffer;
        buffer.writeIndex(0);
        buffer.write(NetworkBuffer.RAW_BYTES, bytes);
        return buffer;
    }

    @Benchmark
    public NetworkBuffer writeBytesLoop() {
        final NetworkBuffer buffer = this.writeBuffer;
        buffer.writeIndex(0);
        for (byte value : bytes) buffer.write(NetworkBuffer.BYTE, value);
        return buffer;
    }

    @Benchmark
    public NetworkBuffer writeShorts() {
        final NetworkBuffer buffer = this.writeBuffer;
        buffer.writeIndex(0);
        buffer.write(NetworkBuffer.RAW_SHORTS, shorts);
        return buffer;
    }

    @Benchmark
    public NetworkBuffer writeShortsLoop() {
        final NetworkBuffer buffer = this.writeBuffer;
        buffer.writeIndex(0);
        for (short value : shorts) buffer.write(NetworkBuffer.SHORT, value);
        return buffer;
    }

    @Benchmark
    public NetworkBuffer writeInts() {
        final NetworkBuffer buffer = this.writeBuffer;
        buffer.writeIndex(0);
        buffer.write(NetworkBuffer.RAW_INTS, ints);
        return buffer;
    }

    @Benchmark
    public NetworkBuffer writeIntsLoop() {
        final NetworkBuffer buffer = this.writeBuffer;
        buffer.writeIndex(0);
        for (int value : ints) buffer.write(NetworkBuffer.INT, value);
        return buffer;
    }

    @Benchmark
    public NetworkBuffer writeLongs() {
        final NetworkBuffer buffer = this.writeBuffer;
        buffer.writeIndex(0);
        buffer.write(NetworkBuffer.RAW_LONGS, longs);
        return buffer;
    }

    @Benchmark
    public NetworkBuffer writeLongsLoop() {
        final NetworkBuffer buffer = this.writeBuffer;
        buffer.writeIndex(0);
        for (long value : longs) buffer.write(NetworkBuffer.LONG, value);
        return buffer;
    }

    @Benchmark
    public NetworkBuffer writeFloats() {
        final NetworkBuffer buffer = this.writeBuffer;
        buffer.writeIndex(0);
        buffer.write(NetworkBuffer.RAW_FLOATS, floats);
        return buffer;
    }

    @Benchmark
    public NetworkBuffer writeFloatsLoop() {
        final NetworkBuffer buffer = this.writeBuffer;
        buffer.writeIndex(0);
        for (float value : floats) buffer.write(NetworkBuffer.FLOAT, value);
        return buffer;
    }

    @Benchmark
    public NetworkBuffer writeDoubles() {
        final NetworkBuffer buffer = this.writeBuffer;
        buffer.writeIndex(0);
        buffer.write(NetworkBuffer.RAW_DOUBLES, doubles);
        return buffer;
    }

    @Benchmark
    public NetworkBuffer writeDoublesLoop() {
        final NetworkBuffer buffer = this.writeBuffer;
        buffer.writeIndex(0);
        for (double value : doubles) buffer.write(NetworkBuffer.DOUBLE, value);
        return buffer;
    }

    @Benchmark
    public byte[] readBytes() {
        final NetworkBuffer buffer = this.readBuffer;
        buffer.readIndex(0);
        return buffer.read(bytesType);
    }

    @Benchmark
    public byte[] readBytesLoop() {
        final NetworkBuffer buffer = this.readBuffer;
        buffer.readIndex(0);
        final byte[] values = new byte[count];
        for (int i = 0; i < values.length; i++) values[i] = buffer.read(NetworkBuffer.BYTE);
        return values;
    }

    @Benchmark
    public short[] readShorts() {
        final NetworkBuffer buffer = this.readBuffer;
        buffer.readIndex(0);
        return buffer.read(shortsType);
    }

    @Benchmark
    public short[] readShortsLoop() {
        final NetworkBuffer buffer = this.readBuffer;
        buffer.readIndex(0);
        final short[] values = new short[count];
        for (int i = 0; i < values.length; i++) values[i] = buffer.read(NetworkBuffer.SHORT);
        return values;
    }

    @Benchmark
    public int[] readInts() {
        final NetworkBuffer buffer = this.readBuffer;
        buffer.readIndex(0);
        return buffer.read(intsType);
    }

    @Benchmark
    public int[] readIntsLoop() {
        final NetworkBuffer buffer = this.readBuffer;
        buffer.readIndex(0);
        final int[] values = new int[count];
        for (int i = 0; i < values.length; i++) values[i] = buffer.read(NetworkBuffer.INT);
        return values;
    }

    @Benchmark
    public long[] readLongs() {
        final NetworkBuffer buffer = this.readBuffer;
        buffer.readIndex(0);
        return buffer.read(longsType);
    }

    @Benchmark
    public long[] readLongsLoop() {
        final NetworkBuffer buffer = this.readBuffer;
        buffer.readIndex(0);
        final long[] values = new long[count];
        for (int i = 0; i < values.length; i++) values[i] = buffer.read(NetworkBuffer.LONG);
        return values;
    }

    @Benchmark
    public float[] readFloats() {
        final NetworkBuffer buffer = this.readBuffer;
        buffer.readIndex(0);
        return buffer.read(floatsType);
    }

    @Benchmark
    public float[] readFloatsLoop() {
        final NetworkBuffer buffer = this.readBuffer;
        buffer.readIndex(0);
        final float[] values = new float[count];
        for (int i = 0; i < values.length; i++) values[i] = buffer.read(NetworkBuffer.FLOAT);
        return values;
    }

    @Benchmark
    public double[] readDoubles() {
        final NetworkBuffer buffer = this.readBuffer;
        buffer.readIndex(0);
        return buffer.read(doublesType);
    }

    @Benchmark
    public double[] readDoublesLoop() {
        final NetworkBuffer buffer = this.readBuffer;
        buffer.readIndex(0);
        final double[] values = new double[count];
        for (int i = 0; i < values.length; i++) values[i] = buffer.read(NetworkBuffer.DOUBLE);
        return values;
    }
}
