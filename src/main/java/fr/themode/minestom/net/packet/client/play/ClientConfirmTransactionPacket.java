package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientConfirmTransactionPacket extends ClientPlayPacket {

    public int windowId;
    public short actionNumber;
    public boolean accepted;

    @Override
    public void read(Buffer buffer) {
        // TODO
    }
}
