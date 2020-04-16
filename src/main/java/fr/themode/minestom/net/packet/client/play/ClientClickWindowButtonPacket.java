package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientClickWindowButtonPacket extends ClientPlayPacket {

    public byte windowId;
    public byte buttonId;

    @Override
    public void read(PacketReader reader) {
        this.windowId = reader.readByte();
        this.buttonId = reader.readByte();
    }
}
