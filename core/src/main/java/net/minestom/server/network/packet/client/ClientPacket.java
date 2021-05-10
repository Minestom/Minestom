package net.minestom.server.network.packet.client;

import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Readable;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a packet received from a client.
 */
public interface ClientPacket extends Readable, Writeable {

    @Override
    default void write(@NotNull BinaryWriter writer) {
        // FIXME: remove when all packets are written and read properly
        throw new UnsupportedOperationException("WIP: This packet is not setup to be written from Minestom code at the moment.");
    }
}
