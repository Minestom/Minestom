package net.minestom.server.network.packet.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketWriting;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.util.function.Supplier;

/**
 * Represents a packet that is only computed when required (either due to memory demand or invalidated data)
 * <p>
 * The cache is stored in a {@link SoftReference} and is invalidated when {@link #invalidate()} is called.
 * <p>
 * Packet supplier must be thread-safe.
 */
@ApiStatus.Internal
public final class CachedPacket implements SendablePacket {
    private final Supplier<ServerPacket> packetSupplier;
    private volatile SoftReference<FramedPacket> packet;

    public CachedPacket(@NotNull Supplier<@NotNull ServerPacket> packetSupplier) {
        this.packetSupplier = packetSupplier;
    }

    public CachedPacket(@NotNull ServerPacket packet) {
        this(() -> packet);
    }

    public void invalidate() {
        this.packet = null;
    }

    public @NotNull ServerPacket packet(@NotNull ConnectionState state) {
        FramedPacket cache = updatedCache(state);
        return cache != null ? cache.packet() : packetSupplier.get();
    }

    public @Nullable NetworkBuffer body(@NotNull ConnectionState state) {
        FramedPacket cache = updatedCache(state);
        return cache != null ? cache.body() : null;
    }

    private @Nullable FramedPacket updatedCache(@NotNull ConnectionState state) {
        if (!ServerFlag.CACHED_PACKET)
            return null;
        SoftReference<FramedPacket> ref = packet;
        FramedPacket cache;
        if (ref == null || (cache = ref.get()) == null) {
            final ServerPacket packet = packetSupplier.get();
            final NetworkBuffer buffer = PacketWriting.allocateTrimmedPacket(state, packet,
                    MinecraftServer.getCompressionThreshold());
            cache = new FramedPacket(packet, buffer);
            this.packet = new SoftReference<>(cache);
        }
        return cache;
    }

    public boolean isValid() {
        final SoftReference<FramedPacket> ref = packet;
        return ref != null && ref.get() != null;
    }

    @Override
    public String toString() {
        final SoftReference<FramedPacket> ref = packet;
        final FramedPacket cache = ref != null ? ref.get() : null;
        return String.format("CachedPacket{cache=%s}", cache);
    }
}
