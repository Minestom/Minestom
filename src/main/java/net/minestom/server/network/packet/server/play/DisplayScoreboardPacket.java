package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class DisplayScoreboardPacket implements ServerPacket {

    public byte position;
    public String scoreName;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeByte(position);
        writer.writeSizedString(scoreName);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DISPLAY_SCOREBOARD;
    }
}
