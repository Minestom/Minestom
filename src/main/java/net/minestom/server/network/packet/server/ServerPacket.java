package net.minestom.server.network.packet.server;

import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.Readable;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a packet which can be sent to a player using {@link PlayerConnection#sendPacket(ServerPacket)}.
 */
public interface ServerPacket extends Readable, Writeable {

    @Override
    default void read(@NotNull BinaryReader reader) {
        // FIXME: remove when all packets are written and read properly
        throw new UnsupportedOperationException("WIP: This packet is not set up to be read from Minestom code at the moment.");
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
