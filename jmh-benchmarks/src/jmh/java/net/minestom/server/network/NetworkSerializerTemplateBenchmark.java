package net.minestom.server.network;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class NetworkSerializerTemplateBenchmark {

    record Packet(long id) {
        private static final NetworkBuffer.Type<Packet> SERIALIZER = 
                NetworkBufferTemplate.template(NetworkBuffer.LONG, Packet::id, Packet::new);
    }
    
    private NetworkBuffer.Type<Packet> serializer;
    private Packet packet;
    private NetworkBuffer readBuffer;
    private NetworkBuffer writeBuffer;

    @Setup
    public void setup() {
        serializer = Packet.SERIALIZER;
        packet = new Packet(0);
        readBuffer = NetworkBuffer.staticBuffer(256);
        readBuffer.write(serializer, new Packet(12451235));
        writeBuffer = NetworkBuffer.staticBuffer(256);
    }

    @Benchmark
    public void writePacket(Blackhole blackhole) {
        var writeBuffer = this.writeBuffer;
        writeBuffer.writeAt(0, serializer, packet);
        blackhole.consume(writeBuffer);
    }

    @Benchmark
    public void readPacket(Blackhole blackhole) {
        blackhole.consume(readBuffer.readAt(0, serializer));
    }

    @TearDown
    public void teardown(Blackhole blackhole) {
        blackhole.consume(serializer);
        blackhole.consume(packet);
        blackhole.consume(readBuffer);
        blackhole.consume(writeBuffer);
    }
}
