package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class KeepAlivePacket implements ServerPacket {

    private long id;

    public KeepAlivePacket(long id) {
        this.id = id;
    }

    @Override
    public void write(Buffer buffer) {
        buffer.putLong(id);
    }

    @Override
    public int getId() {
        return 0x20;
    }
}
