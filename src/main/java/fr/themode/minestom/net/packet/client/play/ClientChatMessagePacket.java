package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientChatMessagePacket extends ClientPlayPacket {

    public String message;

    @Override
    public void read(PacketReader reader) {
        this.message = reader.readSizedString();
    }
}
