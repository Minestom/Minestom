package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class VehicleMovePacket implements ServerPacket {

    public double x, y, z;
    public float yaw, pitch;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeDouble(x);
        writer.writeDouble(y);
        writer.writeDouble(z);
        writer.writeFloat(yaw);
        writer.writeFloat(pitch);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.VEHICLE_MOVE;
    }
}
