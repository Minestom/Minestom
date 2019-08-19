package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class UpdateScorePacket implements ServerPacket {

    public String entityName;
    public byte action;
    public String objectiveName;
    public int value;

    @Override
    public void write(Buffer buffer) {
        Utils.writeString(buffer, entityName);
        buffer.putByte(action);
        Utils.writeString(buffer, objectiveName);
        if (action != 1) {
            Utils.writeVarInt(buffer, value);
        }
    }

    @Override
    public int getId() {
        return 0x4C;
    }
}
