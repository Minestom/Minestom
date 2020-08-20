package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientNameItemPacket extends ClientPlayPacket {

    public String itemName;

    @Override
    public void read(BinaryReader reader) {
        this.itemName = reader.readSizedString();
    }
}
