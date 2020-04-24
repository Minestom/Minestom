package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientKeepAlivePacket extends ClientPlayPacket {

    public long id;

    @Override
    public void read(PacketReader reader) {
        this.id = reader.readLong();
    }
}
