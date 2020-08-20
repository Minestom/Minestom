package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientKeepAlivePacket extends ClientPlayPacket {

    public long id;

    @Override
    public void read(BinaryReader reader) {
        this.id = reader.readLong();
    }
}
