package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class HeldItemChangePacket implements ServerPacket {

    public short slot;

    @Override
    public void write(Buffer buffer) {
        buffer.putShort(slot);
    }

    @Override
    public int getId() {
        return 0x3F;
    }
}
