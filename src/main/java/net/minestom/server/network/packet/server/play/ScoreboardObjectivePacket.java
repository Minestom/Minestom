package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class ScoreboardObjectivePacket implements ServerPacket {

    public String objectiveName;
    public byte mode;
    public ColoredText objectiveValue;
    public int type;

    @Override
    public void write(PacketWriter writer) {
        writer.writeSizedString(objectiveName);
        writer.writeByte(mode);

        if (mode == 0 || mode == 2) {
            writer.writeSizedString(objectiveValue.toString());
            writer.writeVarInt(type);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SCOREBOARD_OBJECTIVE;
    }
}
