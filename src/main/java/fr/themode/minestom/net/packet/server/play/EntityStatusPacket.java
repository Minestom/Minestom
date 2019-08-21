package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class EntityStatusPacket implements ServerPacket {

    public int entityId;
    public byte status;

    @Override
    public void write(Buffer buffer) {
        buffer.putInt(entityId);
        buffer.putByte(status);
    }

    @Override
    public int getId() {
        return 0x1B;
    }
}
