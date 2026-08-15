package net.minestom.server.network.packet;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.common.KeepAlivePacket;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.SampleTime)
@State(Scope.Group)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class NetworkCachedPacketBenchmark {
    static {
        MinecraftServer.init();
    }

    @Param({"1", "1000", "100000"})
    private int packetTime;

    private Random random;
    private ServerPacket packet;
    private CachedPacket cachedPacket;

    @Setup(Level.Iteration)
    public void setup() {
        random = new Random(151243);
        packet = new KeepAlivePacket(0);
        int packetTime = this.packetTime;
        cachedPacket = new CachedPacket(() -> {
            Blackhole.consumeCPU(packetTime);
            return packet;
        });
    }
    @Benchmark
    @Group("shared")
    @GroupThreads(3)
    public void packet(Blackhole blackhole) {
        blackhole.consume(cachedPacket.packet(ConnectionState.PLAY));
    }

    @Benchmark
    @Group("shared")
    @GroupThreads
    public void invalidator() {
        if (random.nextInt(100) < 10) {
            cachedPacket.invalidate();
        }
        Blackhole.consumeCPU(1500);
    }

    @TearDown
    public void teardown(Blackhole blackhole) {
        blackhole.consume(random);
        blackhole.consume(packet);
        blackhole.consume(cachedPacket);
    }
}
