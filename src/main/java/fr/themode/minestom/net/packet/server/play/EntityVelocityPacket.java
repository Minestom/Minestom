package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class EntityVelocityPacket implements ServerPacket {

    public int entityId;
    public short velocityX, velocityY, velocityZ;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
        buffer.putShort(velocityX);
        buffer.putShort(velocityY);
        buffer.putShort(velocityZ);
    }

    @Override
    public int getId() {
        return 0x45;
    }
}
