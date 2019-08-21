package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Utils;

public class EntityTeleportPacket implements ServerPacket {

    public int entityId;
    public Position position;
    public boolean onGround;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
        buffer.putDouble(position.getX());
        buffer.putDouble(position.getY());
        buffer.putDouble(position.getZ());
        buffer.putByte((byte) (position.getYaw() * 256f / 360f));
        buffer.putByte((byte) (position.getPitch() * 256f / 360f));
        buffer.putBoolean(onGround);
    }

    @Override
    public int getId() {
        return 0x56;
    }
}
