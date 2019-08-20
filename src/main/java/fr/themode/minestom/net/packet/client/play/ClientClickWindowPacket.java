package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Utils;

public class ClientClickWindowPacket extends ClientPlayPacket {

    public byte windowId;
    public short slot;
    public byte button;
    public short actionNumber;
    public int mode;
    // TODO clicked item

    @Override
    public void read(Buffer buffer) {
        this.windowId = buffer.getByte();
        this.slot = buffer.getShort();
        this.button = buffer.getByte();
        this.actionNumber = buffer.getShort();
        this.mode = Utils.readVarInt(buffer);
        // TODO read clicked item
    }
}
