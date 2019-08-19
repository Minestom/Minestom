package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class DisplayScoreboardPacket implements ServerPacket {

    public byte position;
    public String scoreName;

    @Override
    public void write(Buffer buffer) {
        buffer.putByte(position);
        Utils.writeString(buffer, scoreName);
    }

    @Override
    public int getId() {
        return 0x42;
    }
}
