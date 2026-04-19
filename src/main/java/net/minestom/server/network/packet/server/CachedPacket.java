package net.minestom.server.network.packet.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.PacketWriting;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents a packet that is only computed when required (either due to memory demand or invalidated data)
 * <p>
 * The cache is stored in a {@link SoftReference} and is invalidated when {@link #invalidate()} is called.
 * <p>
 * Packet supplier must be thread-safe, a result must be non-null, and the same state should be validated.
 */
@ApiStatus.Internal
public final class CachedPacket implements SendablePacket {
    private static final VarHandle PACKET_HANDLE;

    static {
        try {
            PACKET_HANDLE = MethodHandles.lookup().findVarHandle(CachedPacket.class, "packet", SoftReference.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private final Supplier<? extends ServerPacket> packetSupplier;
    @SuppressWarnings("unused") // VarHandle
    private @Nullable SoftReference<FramedPacket> packet;

    public CachedPacket(Supplier<? extends ServerPacket> packetSupplier) {
        this.packetSupplier = packetSupplier;
    }

    public CachedPacket(ServerPacket packet) {
        Objects.requireNonNull(packet, "packet");
        this(() -> packet);
    }

    public void invalidate() {
        if (!ServerFlag.CACHED_PACKET) return;
        PACKET_HANDLE.setRelease(this, null);
    }

    public ServerPacket packet(ConnectionState state, @Nullable PacketParser<ServerPacket> writer) {
        FramedPacket cache = updatedCache(state, writer);
        return cache != null ? cache.packet() : packetSupplier.get();
    }

    public @Nullable NetworkBuffer body(ConnectionState state, @Nullable PacketParser<ServerPacket> writer) {
        FramedPacket cache = updatedCache(state, writer);
        return cache != null ? cache.body() : null;
    }

    private @Nullable FramedPacket updatedCache(ConnectionState state, @Nullable PacketParser<ServerPacket> writer) {
        if (!ServerFlag.CACHED_PACKET) return null;
        // Try to get the cached packet if it has been set.
        // Also, if it hasn't been GC'd
        SoftReference<FramedPacket> ref = getAcquire();
        FramedPacket cache;
        if (ref != null && (cache = ref.get()) != null) return cache;
        // Start the slow path, but first check the writer exists.
        if (writer == null) return null;
        return updateCache(state, writer);
    }

    public boolean isValid() {
        final SoftReference<FramedPacket> ref = getAcquire();
        return ref != null && ref.get() != null;
    }

    @Override
    public String toString() {
        final SoftReference<FramedPacket> ref = getAcquire();
        final FramedPacket cache = ref != null ? ref.get() : null;
        return String.format("CachedPacket{cache=%s}", cache);
    }

    @SuppressWarnings("unchecked")
    private @Nullable SoftReference<FramedPacket> getAcquire() {
        return (SoftReference<FramedPacket>) PACKET_HANDLE.getAcquire(this);
    }

    // Slow cache update, double checked locked
    private synchronized FramedPacket updateCache(ConnectionState state, PacketParser<ServerPacket> writer) {
        SoftReference<FramedPacket> ref = getAcquire();
        FramedPacket cache;
        if (ref != null && (cache = ref.get()) != null) return cache;

        final ServerPacket packet = packetSupplier.get();
        final NetworkBuffer buffer = PacketWriting.allocateTrimmedPacket(writer, state, packet,
                MinecraftServer.getCompressionThreshold());
        cache = new FramedPacket(packet, buffer);
        PACKET_HANDLE.setRelease(this, new SoftReference<>(cache));
        return cache;
    }
}
