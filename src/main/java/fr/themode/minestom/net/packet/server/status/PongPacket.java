package fr.themode.minestom.net.packet.server.status;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class PongPacket implements ServerPacket {

    public long number;

    public PongPacket(long number) {
        this.number = number;
    }

    @Override
    public void write(Buffer buffer) {
        buffer.putLong(number);
    }

    @Override
    public int getId() {
        return 0x01;
    }
}
