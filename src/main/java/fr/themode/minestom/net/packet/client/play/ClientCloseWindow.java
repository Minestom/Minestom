package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientCloseWindow extends ClientPlayPacket {

    public int windowId;

    @Override
    public void read(PacketReader reader) {
        this.windowId = reader.readVarInt();
    }
}
