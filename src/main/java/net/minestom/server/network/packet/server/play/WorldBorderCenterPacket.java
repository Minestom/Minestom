package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record WorldBorderCenterPacket(double x, double z) implements ServerPacket {
    public WorldBorderCenterPacket(BinaryReader reader) {
        this(reader.readDouble(), reader.readDouble());
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
