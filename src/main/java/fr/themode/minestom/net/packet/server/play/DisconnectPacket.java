package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class DisconnectPacket implements ServerPacket {

    private String message;

    public DisconnectPacket(String message) {
        this.message = message;
    }

    @Override
    public void write(Buffer buffer) {
        Utils.writeString(buffer, this.message);
    }

    @Override
    public int getId() {
        return 0x1A;
    }
}
