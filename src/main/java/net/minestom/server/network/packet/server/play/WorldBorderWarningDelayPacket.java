package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class WorldBorderWarningDelayPacket implements ServerPacket {

    public int warningTime;

    public static WorldBorderWarningDelayPacket of(int warningTime) {
        WorldBorderWarningDelayPacket packet = new WorldBorderWarningDelayPacket();
        packet.warningTime = warningTime;
        return packet;
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.warningTime = reader.readVarInt();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(warningTime);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WORLD_BORDER_WARNING_DELAY;
    }
}
