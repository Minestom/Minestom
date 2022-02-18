package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record PlayerPositionAndLookPacket(Pos position, byte flags, int teleportId,
                                          boolean dismountVehicle) implements ServerPacket {
    public PlayerPositionAndLookPacket(BinaryReader reader) {
        this(new Pos(reader.readDouble(), reader.readDouble(), reader.readDouble(), reader.readFloat(), reader.readFloat()),
                reader.readByte(), reader.readVarInt(), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeDouble(position.x());
        writer.writeDouble(position.y());
        writer.writeDouble(position.z());

        writer.writeFloat(position.yaw());
        writer.writeFloat(position.pitch());

        writer.writeByte(flags);
        writer.writeVarInt(teleportId);
        writer.writeBoolean(dismountVehicle);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_POSITION_AND_LOOK;
    }
}