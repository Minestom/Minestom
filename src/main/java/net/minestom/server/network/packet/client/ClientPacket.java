package net.minestom.server.network.packet.client;

import net.minestom.server.network.packet.PacketReader;

public interface ClientPacket {

    void read(PacketReader reader);

}
