package fr.themode.minestom.net.packet.client;

import fr.themode.minestom.net.packet.PacketReader;

public interface ClientPacket {

    void read(PacketReader reader, Runnable callback);

}
