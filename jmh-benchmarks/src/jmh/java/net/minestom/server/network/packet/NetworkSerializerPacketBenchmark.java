package net.minestom.server.network.packet;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.common.KeepAlivePacket;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class NetworkSerializerPacketBenchmark {

    private KeepAlivePacket packet;
    private NetworkBuffer readBuffer;
    private NetworkBuffer writeBuffer;

    @Setup(Level.Iteration)
    public void setup() {
        packet = new KeepAlivePacket(0);
        readBuffer = NetworkBuffer.staticBuffer(256);
        readBuffer.write(KeepAlivePacket.SERIALIZER, new KeepAlivePacket(12451235));
        writeBuffer = NetworkBuffer.staticBuffer(256);
    }

    @Benchmark
    public void writePacket(Blackhole blackhole) {
        writeBuffer.writeAt(0, KeepAlivePacket.SERIALIZER, packet);
        blackhole.consume(writeBuffer);
    }

    @Benchmark
    public void readPacket(Blackhole blackhole) {
        blackhole.consume(readBuffer.readAt(0, KeepAlivePacket.SERIALIZER));
    }

    @TearDown
    public void teardown(Blackhole blackhole) {
        blackhole.consume(packet);
        blackhole.consume(readBuffer);
        blackhole.consume(writeBuffer);
    }
}
