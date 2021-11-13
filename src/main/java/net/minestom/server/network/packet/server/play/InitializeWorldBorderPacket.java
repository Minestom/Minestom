package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public record InitializeWorldBorderPacket(double x, double z,
                                          double oldDiameter, double newDiameter, long speed,
                                          int portalTeleportBoundary, int warningTime,
                                          int warningBlocks) implements ServerPacket {
    public InitializeWorldBorderPacket(BinaryReader reader) {
        this(reader.readDouble(), reader.readDouble(),
                reader.readDouble(), reader.readDouble(),
                reader.readVarLong(), reader.readVarInt(), reader.readVarInt(), reader.readVarInt());
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeDouble(x);
        writer.writeDouble(z);
        writer.writeDouble(oldDiameter);
        writer.writeDouble(newDiameter);
        writer.writeVarLong(speed);
        writer.writeVarInt(portalTeleportBoundary);
        writer.writeVarInt(warningTime);
        writer.writeVarInt(warningBlocks);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.INITIALIZE_WORLD_BORDER;
    }
}
