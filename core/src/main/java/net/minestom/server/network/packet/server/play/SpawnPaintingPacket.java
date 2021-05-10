package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SpawnPaintingPacket implements ServerPacket {

    public int entityId;
    public UUID entityUuid;
    public int motive;
    public BlockPosition position;
    public byte direction;

    public SpawnPaintingPacket() {
        entityUuid = new UUID(0, 0);
        position = new BlockPosition(0, 0, 0);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeUuid(entityUuid);
        writer.writeVarInt(motive);
        writer.writeBlockPosition(position);
        writer.writeByte(direction);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();
        entityUuid = reader.readUuid();
        motive = reader.readVarInt();
        position = reader.readBlockPosition();
        direction = reader.readByte();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_PAINTING;
    }
}
