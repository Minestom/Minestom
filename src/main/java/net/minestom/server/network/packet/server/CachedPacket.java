package net.minestom.server.network.packet.server;

import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
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

    public @NotNull ServerPacket packet() {
        FramedPacket cache = updatedCache();
        return cache != null ? cache.packet() : packetSupplier.get();
    }

    public @Nullable ByteBuffer body() {
        FramedPacket cache = updatedCache();
        return cache != null ? cache.body() : null;
    }

    private @Nullable FramedPacket updatedCache() {
        if (!PacketUtils.CACHED_PACKET)
            return null;
        SoftReference<FramedPacket> ref = packet;
        FramedPacket cache;
        if (ref == null || (cache = ref.get()) == null) {
            cache = PacketUtils.allocateTrimmedPacket(packetSupplier.get());
            this.packet = new SoftReference<>(cache);
        }
        return cache;
    }
}
