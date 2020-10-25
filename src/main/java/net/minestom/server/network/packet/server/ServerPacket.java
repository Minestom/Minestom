package net.minestom.server.network.packet.server;

import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a packet which can be sent to a player using {@link PlayerConnection#sendPacket(ServerPacket)}.
 */
public interface ServerPacket extends Writeable {

    /**
     * Writes the packet to a {@link BinaryWriter}.
     *
     * @param writer the writer to write the packet to.
     */
    void write(@NotNull BinaryWriter writer);

    /**
     * Gets the id of this packet.
     * <p>
     * Written in the final buffer header so it needs to match the client id.
     *
     * @return the id of this packet
     */
    int getId();

}
