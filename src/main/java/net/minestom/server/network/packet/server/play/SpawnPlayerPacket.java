package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SpawnPlayerPacket implements ServerPacket {

    public int entityId;
    public UUID playerUuid;
    public Position position;

    public SpawnPlayerPacket() {
        playerUuid = new UUID(0,0);
        position = new Position();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeUuid(playerUuid);
        writer.writeDouble(position.getX());
        writer.writeDouble(position.getY());
        writer.writeDouble(position.getZ());
        writer.writeByte((byte) (position.getYaw() * 256f / 360f));
        writer.writeByte((byte) (position.getPitch() * 256f / 360f));
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();
        playerUuid = reader.readUuid();
        position = new Position(reader.readDouble(), reader.readDouble(), reader.readDouble());
        position.setYaw((reader.readByte() * 360f) / 256f);
        position.setPitch((reader.readByte() * 360f) / 256f);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_PLAYER;
    }
}
