package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class WindowPropertyPacket implements ServerPacket {

    public byte windowId;
    public short property;
    public short value;

    @Override
    public void write(Buffer buffer) {
        buffer.putByte(windowId);
        buffer.putShort(property);
        buffer.putShort(value);
    }

    @Override
    public int getId() {
        return 0x15;
    }
}
