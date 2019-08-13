package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class ChatMessagePacket implements ServerPacket {

    private String message;
    private Position position;

    public ChatMessagePacket(String message, Position position) {
        this.message = message;
        this.position = position;
    }

    @Override
    public void write(Buffer buffer) {
        Utils.writeString(buffer, this.message);
        buffer.putByte((byte) this.position.ordinal());
    }

    @Override
    public int getId() {
        return 0x0E;
    }

    public enum Position {

        CHAT,
        SYSTEM_MESSAGE,
        GAME_INFO;
    }
}
