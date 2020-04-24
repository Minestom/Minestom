package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class DisplayScoreboardPacket implements ServerPacket {

    public byte position;
    public String scoreName;

    @Override
    public void write(PacketWriter writer) {
        writer.writeByte(position);
        writer.writeSizedString(scoreName);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DISPLAY_SCOREBOARD;
    }
}
