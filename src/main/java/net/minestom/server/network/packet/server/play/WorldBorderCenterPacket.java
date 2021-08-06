package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class WorldBorderCenterPacket implements ServerPacket {

    public double x;
    public double z;

    public static WorldBorderCenterPacket of(double x, double z) {
        WorldBorderCenterPacket packet = new WorldBorderCenterPacket();
        packet.x = x;
        packet.z = z;
        return packet;
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.x = reader.readDouble();
        this.z = reader.readDouble();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeDouble(x);
        writer.writeDouble(z);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WORLD_BORDER_CENTER;
    }
}
