package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientCloseWindow extends ClientPlayPacket {

    public int windowId;

    @Override
    public void read(BinaryReader reader) {
        this.windowId = reader.readVarInt();
    }
}
