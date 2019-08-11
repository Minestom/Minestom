package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class EntityTeleportPacket implements ServerPacket {

    public int entityId;
    public double x, y, z;
    public float yaw, pitch;
    public boolean onGround;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
        buffer.putDouble(x);
        buffer.putDouble(y);
        buffer.putDouble(y);
        buffer.putByte((byte) (this.yaw * 256 / 360));
        buffer.putByte((byte) (this.pitch * 256 / 360));
        buffer.putBoolean(onGround);
    }

    @Override
    public int getId() {
        return 0x56;
    }
}
