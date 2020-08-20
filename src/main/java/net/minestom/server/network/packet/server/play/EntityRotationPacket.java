package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class EntityRotationPacket implements ServerPacket {

    public int entityId;
    public float yaw, pitch;
    public boolean onGround;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeByte((byte) (yaw * 256 / 360));
        writer.writeByte((byte) (pitch * 256 / 360));
        writer.writeBoolean(onGround);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_ROTATION;
    }
}
