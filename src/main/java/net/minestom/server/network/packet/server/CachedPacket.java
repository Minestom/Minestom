package net.minestom.server.network.packet.server;

import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
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
    private static final AtomicIntegerFieldUpdater<CachedPacket> UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(CachedPacket.class, "updated");
    private final Supplier<ServerPacket> packetSupplier;
    // 0 means that the reference needs to be updated
    // Anything else (currently 1) means that the packet is up-to-date
    private volatile int updated = 0;
    private SoftReference<FramedPacket> packet;

    public CachedPacket(@NotNull Supplier<@NotNull ServerPacket> packetSupplier) {
        this.packetSupplier = packetSupplier;
    }

    public CachedPacket(@NotNull ServerPacket packet) {
        this(() -> packet);
    }

    public void invalidate() {
        this.updated = 0;
    }

    public @NotNull ServerPacket packet() {
        FramedPacket cache;
        if (updated == 1 && (cache = packet.get()) != null)
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
        SoftReference<FramedPacket> ref;
        FramedPacket cache;
        if (updated == 0 || ((ref = packet) == null || (cache = ref.get()) == null)) {
            cache = PacketUtils.allocateTrimmedPacket(packetSupplier.get());
            this.packet = new SoftReference<>(cache);
            UPDATER.compareAndSet(this, 0, 1);
        }
        return cache;
    }
}
