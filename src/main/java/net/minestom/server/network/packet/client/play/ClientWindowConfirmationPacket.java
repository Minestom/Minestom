package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientWindowConfirmationPacket extends ClientPlayPacket {

    public byte windowId;
    public short actionNumber;
    public boolean accepted;

    @Override
    public void read(BinaryReader reader) {
        this.windowId = reader.readByte();
        this.actionNumber = reader.readShort();
        this.accepted = reader.readBoolean();
    }
}
