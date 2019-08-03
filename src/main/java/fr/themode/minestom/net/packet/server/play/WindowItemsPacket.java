package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class WindowItemsPacket implements ServerPacket {

    public byte windowId;
    public short count;

    // TODO slot data (Array of Slot)

    @Override
    public void write(Buffer buffer) {
        buffer.putByte(windowId);
        buffer.putShort(count);
        // TODO replace with actual array of slot
        buffer.putBoolean(false); // Not present
    }

    @Override
    public int getId() {
        return 0x15;
    }
}
