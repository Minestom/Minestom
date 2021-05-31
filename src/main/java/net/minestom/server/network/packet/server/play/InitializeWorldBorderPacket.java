package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class InitializeWorldBorderPacket implements ServerPacket {

    public double x, z;
    public double oldDiameter;
    public double newDiameter;
    public long speed;
    public int portalTeleportBoundary;
    public int warningTime;
    public int warningBlocks;

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
    public void read(@NotNull BinaryReader reader) {
        this.x = reader.readDouble();
        this.z = reader.readDouble();
        this.oldDiameter = reader.readDouble();
        this.newDiameter = reader.readDouble();
        this.speed = reader.readVarLong();
        this.portalTeleportBoundary = reader.readVarInt();
        this.warningTime = reader.readVarInt();
        this.warningBlocks = reader.readVarInt();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.INITIALIZE_WORLD_BORDER;
    }
}
