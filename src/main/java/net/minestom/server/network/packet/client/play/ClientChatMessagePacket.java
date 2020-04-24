package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientChatMessagePacket extends ClientPlayPacket {

    public String message;

    @Override
    public void read(PacketReader reader) {
        this.message = reader.readSizedString();
    }
}
