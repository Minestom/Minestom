package net.minestom.server.network.packet.server;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents a packet that is lazily allocated. Potentially in a different thread.
 * <p>
 * The supplier will be called at most once, the first time {@link #packet()} is called.
 */
@ApiStatus.Internal
public final class LazyPacket implements SendablePacket {
    private static final VarHandle PACKET_HANDLE;

    static {
        try {
            PACKET_HANDLE = MethodHandles.lookup().findVarHandle(LazyPacket.class, "packet", ServerPacket.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    // To be replaced by StabeValue/LazyConstant when available.
    // Cleared once called. (voltiale for simplicity)
    private volatile @Nullable Supplier<ServerPacket> packetSupplier;
    @SuppressWarnings("unused") // VarHandle
    private @Nullable ServerPacket packet;

    public LazyPacket(Supplier<ServerPacket> packetSupplier) {
        this.packetSupplier = packetSupplier;
    }

    public ServerPacket packet() {
        ServerPacket packet = getAcquire();
        if (packet == null) {
            synchronized (this) {
                if ((packet = getAcquire()) == null) {
                    Supplier<ServerPacket> packetSupplier = this.packetSupplier;
                    assert packetSupplier != null;
                    packet = Objects.requireNonNull(packetSupplier.get(), "packetSupplier returned null");
                    PACKET_HANDLE.setRelease(this, packet);
                    this.packetSupplier = null;
                }
            }
        }
        return packet;
    }

    private @Nullable ServerPacket getAcquire() {
        return (ServerPacket) PACKET_HANDLE.getAcquire(this);
    }

    @Override
    public String toString() {
        return String.format("LazyPacket{packet=%s}", packet());
    }
}
