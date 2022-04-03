package net.minestom.server.network.packet.server;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.VarHandle;
import java.util.function.Supplier;

/**
 * Represents a packet that is lazily allocated. Potentially in a different thread.
 * <p>
 * Supplier must be thread-safe.
 */
@ApiStatus.Internal
public final class LazyPacket implements SendablePacket {
    private final Supplier<ServerPacket> packetSupplier;
    private ServerPacket packet;

    public LazyPacket(@NotNull Supplier<@NotNull ServerPacket> packetSupplier) {
        this.packetSupplier = packetSupplier;
    }

    public @NotNull ServerPacket packet() {
        VarHandle.acquireFence();
        ServerPacket packet = this.packet;
        if (packet == null) {
            synchronized (this) {
                packet = this.packet;
                if (packet == null) {
                    this.packet = packet = packetSupplier.get();
                }
            }
        }
        return packet;
    }
}
