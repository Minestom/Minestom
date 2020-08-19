package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientChatMessagePacket extends ClientPlayPacket {

    public String message;

    @Override
    public void read(BinaryReader reader) {
        this.message = reader.readSizedString();
    }
}
