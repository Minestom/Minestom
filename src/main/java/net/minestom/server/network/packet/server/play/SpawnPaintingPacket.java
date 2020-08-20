package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.UUID;

public class SpawnPaintingPacket implements ServerPacket {

    public int entityId;
    public UUID entityUuid;
    public int motive;
    public BlockPosition position;
    public byte direction;

    @Override
    public void write(BinaryWriter writer) {
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
