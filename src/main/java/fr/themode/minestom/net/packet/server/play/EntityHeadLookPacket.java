package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class EntityHeadLookPacket implements ServerPacket {

    public int entityId;
    public float yaw;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
        buffer.putByte((byte) (this.yaw * 256 / 360));
    }

    @Override
    public int getId() {
        return 0x3B;
    }
}
