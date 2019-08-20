package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Utils;

public class ClientPluginMessagePacket extends ClientPlayPacket {

    private String identifier;
    private byte[] data;

    @Override
    public void read(Buffer buffer) {
        this.identifier = Utils.readString(buffer);
        this.data = buffer.getAllBytes();
    }
}
