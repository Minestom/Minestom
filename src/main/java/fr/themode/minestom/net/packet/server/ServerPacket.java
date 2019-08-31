package fr.themode.minestom.net.packet.server;

import fr.themode.minestom.net.packet.PacketWriter;

public interface ServerPacket {

    void write(PacketWriter writer);

    int getId();

}
