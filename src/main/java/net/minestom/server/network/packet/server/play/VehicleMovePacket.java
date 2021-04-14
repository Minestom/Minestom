package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class VehicleMovePacket implements ServerPacket {

    public double x, y, z;
    public float yaw, pitch;

    /**
     * Default constructor, required for reflection operations.
     */
    public VehicleMovePacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeDouble(x);
        writer.writeDouble(y);
        writer.writeDouble(z);
        writer.writeFloat(yaw);
        writer.writeFloat(pitch);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        x = reader.readDouble();
        y = reader.readDouble();
        z = reader.readDouble();
        yaw = reader.readFloat();
        pitch = reader.readFloat();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.VEHICLE_MOVE;
    }
}
