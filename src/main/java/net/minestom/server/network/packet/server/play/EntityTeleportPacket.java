package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record EntityTeleportPacket(int entityId, Pos position, boolean onGround) implements ServerPacket {
    public EntityTeleportPacket(BinaryReader reader) {
        this(reader.readVarInt(), new Pos(reader.readDouble(), reader.readDouble(), reader.readDouble(),
                        reader.readByte() * 360f / 256f, reader.readByte() * 360f / 256f),
                reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeDouble(position.x());
        writer.writeDouble(position.y());
        writer.writeDouble(position.z());
        writer.writeByte((byte) (position.yaw() * 256f / 360f));
        writer.writeByte((byte) (position.pitch() * 256f / 360f));
        writer.writeBoolean(onGround);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_TELEPORT;
    }
}
