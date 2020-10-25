package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class TimeUpdatePacket implements ServerPacket {

    public long worldAge;
    public long timeOfDay;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeLong(worldAge);
        writer.writeLong(timeOfDay);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TIME_UPDATE;
    }
}
