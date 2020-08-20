package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientClickWindowButtonPacket extends ClientPlayPacket {

    public byte windowId;
    public byte buttonId;

    @Override
    public void read(BinaryReader reader) {
        this.windowId = reader.readByte();
        this.buttonId = reader.readByte();
    }
}
