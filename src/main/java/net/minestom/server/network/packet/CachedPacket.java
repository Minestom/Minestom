package net.minestom.server.network.packet;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.ApiStatus;

import java.lang.ref.SoftReference;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class CachedPacket {
    private final Supplier<ServerPacket> supplier;
    private volatile long lastChange;
    private volatile long packetTimestamp;
    private SoftReference<FramedPacket> packet;

    public CachedPacket(Supplier<ServerPacket> supplier) {
        this.supplier = supplier;
    }

    public void updateTimestamp() {
        this.lastChange = System.currentTimeMillis();
    }

    public FramedPacket retrieve() {
        final long lastChange = this.lastChange;
        final long timestamp = packetTimestamp;
        final var ref = packet;
        FramedPacket cache = ref != null ? ref.get() : null;
        if (cache == null || lastChange > timestamp) {
            cache = PacketUtils.allocateTrimmedPacket(supplier.get());
            this.packet = new SoftReference<>(cache);
            this.packetTimestamp = lastChange;
        }
        return cache;
    }

    public long lastChange() {
        return lastChange;
    }
}
