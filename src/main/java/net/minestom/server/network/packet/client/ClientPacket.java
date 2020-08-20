package net.minestom.server.network.packet.client;

import net.minestom.server.utils.binary.BinaryReader;

public interface ClientPacket {

    void read(BinaryReader reader);

}
