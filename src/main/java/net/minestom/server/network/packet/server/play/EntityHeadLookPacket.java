package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class EntityHeadLookPacket implements ServerPacket {

    public int entityId;
    public float yaw;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeByte((byte) (this.yaw * 256 / 360));
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_HEAD_LOOK;
    }
}
