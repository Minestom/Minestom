package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientClickWindowButtonPacket extends ClientPlayPacket {

    public byte windowId;
    public byte buttonId;

    @Override
    public void read(PacketReader reader) {
        this.windowId = reader.readByte();
        this.buttonId = reader.readByte();
    }
}
