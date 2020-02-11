package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class VehicleMovePacket implements ServerPacket {

    public double x, y, z;
    public float yaw, pitch;

    @Override
    public void write(PacketWriter writer) {
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
