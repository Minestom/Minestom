package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record WorldBorderLerpSizePacket(double oldDiameter, double newDiameter, long speed) implements ServerPacket {
    public WorldBorderLerpSizePacket(BinaryReader reader) {
        this(reader.readDouble(), reader.readDouble(), reader.readVarLong());
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
