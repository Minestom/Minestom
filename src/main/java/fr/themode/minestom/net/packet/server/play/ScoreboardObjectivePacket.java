package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class ScoreboardObjectivePacket implements ServerPacket {

    public String objectiveName;
    public byte mode;
    public String objectiveValue;
    public int type;

    @Override
    public void write(Buffer buffer) {
        Utils.writeString(buffer, objectiveName);
        buffer.putByte(mode);
        if (mode == 0 || mode == 2) {
            Utils.writeString(buffer, objectiveValue);
            Utils.writeVarInt(buffer, type);
        }
    }

    @Override
    public int getId() {
        return 0x49;
    }
}
