package net.minestom.server.network.packet.server;

import net.minestom.server.utils.binary.BinaryWriter;

public interface ServerPacket {

    void write(BinaryWriter writer);

    int getId();

}
