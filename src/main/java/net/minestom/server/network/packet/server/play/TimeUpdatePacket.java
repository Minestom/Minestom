package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class TimeUpdatePacket implements ServerPacket {

    public long worldAge;
    public long timeOfDay;

    @Override
    public void write(PacketWriter writer) {
        writer.writeLong(worldAge);
        writer.writeLong(timeOfDay);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TIME_UPDATE;
    }
}
