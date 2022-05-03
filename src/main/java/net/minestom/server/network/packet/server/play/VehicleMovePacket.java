package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record VehicleMovePacket(@NotNull Pos position) implements ServerPacket {
    public VehicleMovePacket(BinaryReader reader) {
        this(new Pos(reader.readDouble(), reader.readDouble(), reader.readDouble(),
                reader.readFloat(), reader.readFloat()));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeDouble(position.x());
        writer.writeDouble(position.y());
        writer.writeDouble(position.z());
        writer.writeFloat(position.yaw());
        writer.writeFloat(position.pitch());
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.VEHICLE_MOVE;
    }
}
