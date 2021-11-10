package net.minestom.server.network.packet;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class CachedPacket {
    private static final AtomicIntegerFieldUpdater<CachedPacket> UPDATER = AtomicIntegerFieldUpdater.newUpdater(CachedPacket.class, "updated");
    private final Supplier<ServerPacket> supplier;
    // 0 means that the reference needs to be updated
    // Anything else (currently 1) means that the packet is up-to-date
    private volatile int updated = 0;
    private SoftReference<FramedPacket> packet;

    public CachedPacket(@NotNull Supplier<@NotNull ServerPacket> supplier) {
        this.supplier = supplier;
    }

    public void invalidate() {
        this.updated = 0;
    }

    public @NotNull FramedPacket retrieve() {
        SoftReference<FramedPacket> ref;
        FramedPacket cache;
        if (updated == 0 ||
                ((ref = packet) == null ||
                        (cache = ref.get()) == null)) {
            cache = PacketUtils.allocateTrimmedPacket(supplier.get());
            this.packet = new SoftReference<>(cache);
            UPDATER.compareAndSet(this, 0, 1);
        }
        return cache;
    }
}
