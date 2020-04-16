package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientWindowConfirmationPacket extends ClientPlayPacket {

    public byte windowId;
    public short actionNumber;
    public boolean accepted;

    @Override
    public void read(PacketReader reader) {
        this.windowId = reader.readByte();
        this.actionNumber = reader.readShort();
        this.accepted = reader.readBoolean();
    }
}
