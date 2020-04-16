package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientKeepAlivePacket extends ClientPlayPacket {

    public long id;

    @Override
    public void read(PacketReader reader) {
        this.id = reader.readLong();
    }
}
