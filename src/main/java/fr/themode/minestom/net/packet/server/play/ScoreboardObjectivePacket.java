package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.chat.Chat;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class ScoreboardObjectivePacket implements ServerPacket {

    public String objectiveName;
    public byte mode;
    public String objectiveValue;
    public int type;

    @Override
    public void write(PacketWriter writer) {
        writer.writeSizedString(objectiveName);
        writer.writeByte(mode);

        if (mode == 0 || mode == 2) {
            writer.writeSizedString(Chat.legacyTextString(objectiveValue));
            writer.writeVarInt(type);
        }
    }

    @Override
    public int getId() {
        return 0x49;
    }
}
