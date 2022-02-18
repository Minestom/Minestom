package net.minestom.server.network.packet.server;

import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
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
    private static final VarHandle PACKET;

    static {
        try {
            PACKET = MethodHandles.lookup().findVarHandle(CachedPacket.class, "packet", SoftReference.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private final Supplier<ServerPacket> packetSupplier;
    @SuppressWarnings("unused")
    private SoftReference<FramedPacket> packet;

    public CachedPacket(@NotNull Supplier<@NotNull ServerPacket> packetSupplier) {
        this.packetSupplier = packetSupplier;
    }

    public CachedPacket(@NotNull ServerPacket packet) {
        this(() -> packet);
    }

    public void invalidate() {
        PACKET.setRelease(this, null);
    }

    public @NotNull ServerPacket packet() {
        @SuppressWarnings("unchecked")
        SoftReference<FramedPacket> ref = (SoftReference<FramedPacket>) PACKET.getAcquire(this);
        FramedPacket cache;
        if (ref != null && (cache = ref.get()) != null)
            return cache.packet(); // Avoid potential packet allocation
        return packetSupplier.get();
    }

    public @NotNull ByteBuffer body() {
        FramedPacket cache = updatedCache();
        return cache != null ? cache.body() : PacketUtils.createFramedPacket(packetSupplier.get());
    }

    private @Nullable FramedPacket updatedCache() {
        if (!PacketUtils.CACHED_PACKET)
            return null;
        @SuppressWarnings("unchecked")
        SoftReference<FramedPacket> ref = (SoftReference<FramedPacket>) PACKET.getAcquire(this);
        FramedPacket cache;
        if (ref == null || (cache = ref.get()) == null) {
            cache = PacketUtils.allocateTrimmedPacket(packetSupplier.get());
            PACKET.setRelease(this, new SoftReference<>(cache));
        }
        return cache;
    }
}
