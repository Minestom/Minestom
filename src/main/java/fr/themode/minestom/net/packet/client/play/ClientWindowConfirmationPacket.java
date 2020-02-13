package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientWindowConfirmationPacket extends ClientPlayPacket {

    public byte windowId;
    public short actionNumber;
    public boolean accepted;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readByte(value -> windowId = value);
        reader.readShort(value -> actionNumber = value);
        reader.readBoolean(value -> {
            accepted = value;
            callback.run();
        });
    }
}
