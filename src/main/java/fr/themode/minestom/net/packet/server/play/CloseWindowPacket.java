package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class CloseWindowPacket implements ServerPacket {

    public int windowId;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, windowId);
    }

    @Override
    public int getId() {
        return 0x13;
    }
}
