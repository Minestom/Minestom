package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientNameItemPacket extends ClientPlayPacket {

    public String itemName;

    @Override
    public void read(PacketReader reader) {
        this.itemName = reader.readSizedString();
    }
}
