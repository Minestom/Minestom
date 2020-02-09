package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class ChatMessagePacket implements ServerPacket {

    private String message;
    private Position position;

    public ChatMessagePacket(String message, Position position) {
        this.message = message;
        this.position = position;
    }

    @Override
    public void write(PacketWriter writer) {
        writer.writeSizedString(message);
        writer.writeByte((byte) position.ordinal());
    }

    @Override
    public int getId() {
        return 0x0F;
    }

    public enum Position {

        CHAT,
        SYSTEM_MESSAGE,
        GAME_INFO;
    }
}
