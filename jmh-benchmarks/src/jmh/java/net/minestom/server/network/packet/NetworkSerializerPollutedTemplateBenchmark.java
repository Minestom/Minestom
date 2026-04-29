package net.minestom.server.network.packet;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.common.KeepAlivePacket;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

import static net.minestom.server.network.NetworkBuffer.*;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class NetworkSerializerPollutedTemplateBenchmark {

    private NetworkBuffer.Type<KeepAlivePacket> keepAliveSerializer;
    private KeepAlivePacket packet;
    private NetworkBuffer readBuffer;
    private NetworkBuffer writeBuffer;

    private Polluter<?>[] polluters;

    @Setup(Level.Trial)
    public void setupTrial() {
        polluters = new Polluter[]{
                polluter(NetworkBufferTemplate.template(BOOLEAN, BooleanPacket::value, BooleanPacket::new), new BooleanPacket(true)),
                polluter(NetworkBufferTemplate.template(BYTE, BytePacket::value, BytePacket::new), new BytePacket((byte) 1)),
                polluter(NetworkBufferTemplate.template(SHORT, ShortPacket::value, ShortPacket::new), new ShortPacket((short) 2)),
                polluter(NetworkBufferTemplate.template(INT, IntPacket::value, IntPacket::new), new IntPacket(3)),
                polluter(NetworkBufferTemplate.template(FLOAT, FloatPacket::value, FloatPacket::new), new FloatPacket(4.0f)),
                polluter(NetworkBufferTemplate.template(DOUBLE, DoublePacket::value, DoublePacket::new), new DoublePacket(5.0d)),
                polluter(NetworkBufferTemplate.template(STRING, StringPacket::value, StringPacket::new), new StringPacket("polluted")),
                polluter(NetworkBufferTemplate.template(VAR_INT, VarIntPacket::value, VarIntPacket::new), new VarIntPacket(6)),
                polluter(NetworkBufferTemplate.template(VAR_LONG, VarLongPacket::value, VarLongPacket::new), new VarLongPacket(7L)),
        };
        keepAliveSerializer = KeepAlivePacket.SERIALIZER;
    }

    @Setup(Level.Iteration)
    public void setupIteration() {
        packet = new KeepAlivePacket(12451235L);
        readBuffer = NetworkBuffer.staticBuffer(256);
        readBuffer.write(keepAliveSerializer, packet);
        writeBuffer = NetworkBuffer.staticBuffer(256);

        for (int i = 0; i < 20_000; i++) {
            Polluter<?> polluter = polluters[i % polluters.length];
            polluter.write();
            polluter.read();
        }
    }

    @Benchmark
    public void writePacket(Blackhole blackhole) {
        var writeBuffer = this.writeBuffer;
        writeBuffer.writeIndex(0);
        keepAliveSerializer.write(writeBuffer, packet);
        blackhole.consume(writeBuffer);
    }

    @Benchmark
    public void readPacket(Blackhole blackhole) {
        var readBuffer = this.readBuffer;
        readBuffer.readIndex(0);
        blackhole.consume(keepAliveSerializer.read(readBuffer));
    }

    @TearDown
    public void teardown(Blackhole blackhole) {
        blackhole.consume(packet);
        blackhole.consume(readBuffer);
        blackhole.consume(writeBuffer);
        blackhole.consume(polluters);
    }

    private static <T> Polluter<T> polluter(NetworkBuffer.Type<T> type, T value) {
        return new Polluter<>(type, value);
    }

    private static final class Polluter<T> {
        private final NetworkBuffer.Type<T> type;
        private final T value;
        private final NetworkBuffer buffer;

        private Polluter(NetworkBuffer.Type<T> type, T value) {
            this.type = type;
            this.value = value;
            this.buffer = NetworkBuffer.staticBuffer(256);
            super();
            write();
        }

        private void write() {
            buffer.writeIndex(0);
            type.write(buffer, value);
        }

        private T read() {
            buffer.readIndex(0);
            return type.read(buffer);
        }
    }

    private record BooleanPacket(boolean value) {
    }

    private record BytePacket(byte value) {
    }

    private record ShortPacket(short value) {
    }

    private record IntPacket(int value) {
    }

    private record FloatPacket(float value) {
    }

    private record DoublePacket(double value) {
    }

    private record StringPacket(String value) {
    }

    private record VarIntPacket(int value) {
    }

    private record VarLongPacket(long value) {
    }
}
