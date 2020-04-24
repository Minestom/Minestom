package net.minestom.server.network.packet.server;

import net.minestom.server.network.packet.PacketWriter;

public interface ServerPacket {

    void write(PacketWriter writer);

    int getId();

}
