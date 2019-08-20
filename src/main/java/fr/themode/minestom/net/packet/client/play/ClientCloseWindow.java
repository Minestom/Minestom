package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Utils;

public class ClientCloseWindow extends ClientPlayPacket {

    public int windowId;

    @Override
    public void read(Buffer buffer) {
        this.windowId = Utils.readVarInt(buffer);
    }
}
