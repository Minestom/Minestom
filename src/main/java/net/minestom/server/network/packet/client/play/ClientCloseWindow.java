package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientCloseWindow extends ClientPlayPacket {

    public int windowId;

    @Override
    public void read(PacketReader reader) {
        this.windowId = reader.readVarInt();
    }
}
