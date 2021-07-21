package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class PlayerPositionAndLookPacket implements ServerPacket {

    public Pos position;
    public byte flags;
    public int teleportId;
    public boolean dismountVehicle;

    public PlayerPositionAndLookPacket(Pos position, byte flags, int teleportId, boolean dismountVehicle) {
        this.position = position;
        this.flags = flags;
        this.teleportId = teleportId;
        this.dismountVehicle = dismountVehicle;
    }

    public PlayerPositionAndLookPacket() {
        this(Pos.ZERO, (byte) 0, 0, false);
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
    public void read(@NotNull BinaryReader reader) {
        position = new Pos(reader.readDouble(), reader.readDouble(), reader.readDouble(), reader.readFloat(), reader.readFloat());

        flags = reader.readByte();
        teleportId = reader.readVarInt();
        dismountVehicle = reader.readBoolean();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_POSITION_AND_LOOK;
    }
}