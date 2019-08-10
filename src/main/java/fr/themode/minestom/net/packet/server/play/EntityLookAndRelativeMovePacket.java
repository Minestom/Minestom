package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class EntityLookAndRelativeMovePacket implements ServerPacket {

    public int entityId;
    public short deltaX, deltaY, deltaZ;
    public float yaw, pitch;
    public boolean onGround;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
        buffer.putShort(deltaX);
        buffer.putShort(deltaY);
        buffer.putShort(deltaZ);
        buffer.putFloat(yaw);
        buffer.putFloat(pitch);
        buffer.putBoolean(onGround);
    }

    @Override
    public int getId() {
        return 0x29;
    }
}
