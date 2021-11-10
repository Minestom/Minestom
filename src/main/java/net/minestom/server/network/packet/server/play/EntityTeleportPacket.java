package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class EntityTeleportPacket implements ServerPacket {

    public int entityId;
    public Pos position;
    public boolean onGround;

    public EntityTeleportPacket(int entityId, Pos position, boolean onGround) {
        this.entityId = entityId;
        this.position = position;
        this.onGround = onGround;
    }

    public EntityTeleportPacket() {
        this(0, Pos.ZERO, false);
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
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();
        position = new Pos(
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
