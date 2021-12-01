package net.minestom.server.network.packet.server;

import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Supplier;

/**
 * Represents a packet that is compute only when required (either due to memory demand or invalidated data)
 * <p>
 * The {@link FramedPacket} is stored in a {@link SoftReference} and is invalidated when the {@link #invalidate()} method is called.
 * <p>
 * The {@link ServerPacket} may be computed dynamically in another thread, or be a constant to potentially
 * improve retrieval performance at the cost of the object staying in the heap.
 */
@ApiStatus.Internal
public final class CachedPacket implements SendablePacket {
    private static final AtomicIntegerFieldUpdater<CachedPacket> UPDATER = AtomicIntegerFieldUpdater.newUpdater(CachedPacket.class, "updated");
    private final Supplier<ServerPacket> packetSupplier;
    // 0 means that the reference needs to be updated
    // Anything else (currently 1) means that the packet is up-to-date
    private volatile int updated = 0;
    private SoftReference<FramedPacket> packet;

    /**
     * Creates a new cached packet that will force the allocation of a new {@link ServerPacket}
     * during invalidation due to memory constraint or invalidation.
     * <p>
     * {@code packetSupplier} must be thread-safe.
     */
    public CachedPacket(@NotNull Supplier<@NotNull ServerPacket> packetSupplier) {
        this.packetSupplier = packetSupplier;
    }

    /**
     * Creates a new cached packet from a constant {@link ServerPacket}
     * which will be the return value of {@link #packet()}.
     */
    public CachedPacket(@NotNull ServerPacket packet) {
        this(() -> packet);
    }

    public void invalidate() {
        this.updated = 0;
    }

    /**
     * Retrieves the packet object without allocating a buffer.
     * <p>
     * This method can be useful in case the payload is not important (e.g. for packet listening),
     * but {@link #retrieve()} should be privileged otherwise.
     */
    public @NotNull ServerPacket packet() {
        return packetSupplier.get();
    }

    /**
     * Retrieves the packet content. May be recomputed if the packet is invalidated or
     * if there is memory demand (handled by a soft reference).
     * <p>
     * {@link FramedPacket#body()} will contain a buffer allocated by this method.
     */
    public @NotNull FramedPacket retrieve() {
        if (!PacketUtils.CACHED_PACKET) {
            // TODO: Using a local buffer may be possible
            return PacketUtils.allocateTrimmedPacket(packet());
        }
        SoftReference<FramedPacket> ref;
        FramedPacket cache;
        if (updated == 0 ||
                ((ref = packet) == null ||
                        (cache = ref.get()) == null)) {
            cache = PacketUtils.allocateTrimmedPacket(packet());
            this.packet = new SoftReference<>(cache);
            UPDATER.compareAndSet(this, 0, 1);
        }
        return cache;
    }
}
