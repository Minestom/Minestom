package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class WorldBorderWarningReachPacket implements ServerPacket {

    public int warningBlocks;

    public static WorldBorderWarningReachPacket of(int warningBlocks) {
        WorldBorderWarningReachPacket packet = new WorldBorderWarningReachPacket();
        packet.warningBlocks = warningBlocks;
        return packet;
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.warningBlocks = reader.readVarInt();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(warningBlocks);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WORLD_BORDER_WARNING_REACH;
    }
}
