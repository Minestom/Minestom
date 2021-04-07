package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class PlayerPositionAndLookPacket implements ServerPacket {

    public Position position;
    public byte flags;
    public int teleportId;

    public PlayerPositionAndLookPacket() {
        position = new Position();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeDouble(position.getX());
        writer.writeDouble(position.getY());
        writer.writeDouble(position.getZ());

        writer.writeFloat(position.getYaw());
        writer.writeFloat(position.getPitch());

        writer.writeByte(flags);
        writer.writeVarInt(teleportId);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        position = new Position(reader.readDouble(), reader.readDouble(), reader.readDouble(), reader.readFloat(), reader.readFloat());

        flags = reader.readByte();
        teleportId = reader.readVarInt();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_POSITION_AND_LOOK;
    }
}