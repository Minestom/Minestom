package net.minestom.server.network.packet.server;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.Supplier;

/**
 * Represents a packet that is lazily allocated. Potentially in a different thread.
 * <p>
 * Supplier must be thread-safe.
 */
@ApiStatus.Internal
public final class LazyPacket implements SendablePacket {
    private static final VarHandle PACKET;

    static {
        try {
            PACKET = MethodHandles.lookup().findVarHandle(LazyPacket.class, "packet", ServerPacket.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private final Supplier<ServerPacket> packetSupplier;
    @SuppressWarnings("unused")
    private ServerPacket packet;

    public LazyPacket(@NotNull Supplier<@NotNull ServerPacket> packetSupplier) {
        this.packetSupplier = packetSupplier;
    }

    public @NotNull ServerPacket packet() {
        ServerPacket packet = (ServerPacket) PACKET.getAcquire(this);
        if (packet == null) {
            synchronized (this) {
                packet = (ServerPacket) PACKET.getAcquire(this);
                if (packet == null) {
                    packet = packetSupplier.get();
                    PACKET.setRelease(this, packet);
                }
            }
        }
        return packet;
    }
}
