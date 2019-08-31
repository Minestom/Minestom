package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientConfirmTransactionPacket extends ClientPlayPacket {

    public int windowId;
    public short actionNumber;
    public boolean accepted;

    @Override
    public void read(PacketReader reader) {
        // TODO
    }
}
