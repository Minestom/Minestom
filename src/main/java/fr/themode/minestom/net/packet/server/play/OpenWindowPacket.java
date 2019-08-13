package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class OpenWindowPacket implements ServerPacket {

    public int windowId;
    public int windowType;
    public String title;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, windowId);
        Utils.writeVarInt(buffer, windowType);
        Utils.writeString(buffer, "{\"text\": \"" + title + " \"}");
    }

    @Override
    public int getId() {
        return 0x2E;
    }
}
