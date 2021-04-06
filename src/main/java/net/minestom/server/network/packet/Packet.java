package net.minestom.server.network.packet;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Readable;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

public interface Packet extends Readable, Writeable {

    @Override
    default void write(@NotNull BinaryWriter writer) {
        throw new UnsupportedOperationException("WIP: This packet is not setup to be written from Minestom code at the moment.");
    }

    @Override
    default void read(@NotNull BinaryReader reader) {
        throw new UnsupportedOperationException("WIP: This packet is not setup to be read from Minestom code at the moment.");
    }

    /**
     * Gets the id of this packet.
     * <p>
     * Written in the final buffer header so it needs to match the client id.
     *
     * @return the id of this packet
     */
    int getId();

}
