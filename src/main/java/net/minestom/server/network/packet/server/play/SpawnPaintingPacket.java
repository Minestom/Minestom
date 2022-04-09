package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record SpawnPaintingPacket(int entityId, @NotNull UUID entityUuid, int motive,
                                  @NotNull Point position, byte direction) implements ServerPacket {
    public SpawnPaintingPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readUuid(), reader.readVarInt(),
                reader.readBlockPosition(), reader.readByte());
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
    public int getId() {
        return ServerPacketIdentifier.SPAWN_PAINTING;
    }
}
