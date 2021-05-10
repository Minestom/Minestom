package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class EntityTeleportPacket implements ServerPacket {

    public int entityId;
    public Position position;
    public boolean onGround;

    public EntityTeleportPacket() {
        position = new Position();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeDouble(position.getX());
        writer.writeDouble(position.getY());
        writer.writeDouble(position.getZ());
        writer.writeByte((byte) (position.getYaw() * 256f / 360f));
        writer.writeByte((byte) (position.getPitch() * 256f / 360f));
        writer.writeBoolean(onGround);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();
        position = new Position(
                reader.readDouble(),
                reader.readDouble(),
                reader.readDouble(),
                reader.readByte() * 360f / 256f,
                reader.readByte() * 360f / 256f
                );
        onGround = reader.readBoolean();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_TELEPORT;
    }
}
