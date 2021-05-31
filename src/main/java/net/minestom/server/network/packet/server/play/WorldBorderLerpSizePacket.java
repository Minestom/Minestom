package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class WorldBorderLerpSizePacket implements ServerPacket {

    public double oldDiameter;
    public double newDiameter;
    public long speed;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.oldDiameter = reader.readDouble();
        this.newDiameter = reader.readDouble();
        this.speed = reader.readVarLong();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeDouble(oldDiameter);
        writer.writeDouble(newDiameter);
        writer.writeVarLong(speed);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WORLD_BORDER_LERP_SIZE;
    }
}
