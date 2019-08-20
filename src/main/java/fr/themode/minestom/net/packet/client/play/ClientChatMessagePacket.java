package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Utils;

public class ClientChatMessagePacket extends ClientPlayPacket {

    public String message;

    @Override
    public void read(Buffer buffer) {
        this.message = Utils.readString(buffer);
    }
}
