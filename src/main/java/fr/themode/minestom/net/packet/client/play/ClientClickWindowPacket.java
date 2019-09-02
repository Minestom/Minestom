package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientClickWindowPacket extends ClientPlayPacket {

    public byte windowId;
    public short slot;
    public byte button;
    public short actionNumber;
    public int mode;
    // TODO clicked item

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readByte(value -> windowId = value);
        reader.readShort(value -> slot = value);
        reader.readByte(value -> button = value);
        reader.readShort(value -> actionNumber = value);
        reader.readVarInt(value -> {
            mode = value;
            callback.run();
        });
        // TODO read clicked item
    }
}
