package net.minestom.server.network.packet.server;

import net.minestom.server.utils.binary.BinaryWriter;

public interface ServerPacket {

    /**
     * Write the packet to a {@link BinaryWriter}
     *
     * @param writer the writer to write the packet to
     */
    void write(BinaryWriter writer);

    /**
     * Get the id of this packet
     * <p>
     * Should be a constant
     *
     * @return the id of this packet
     */
    int getId();

}
