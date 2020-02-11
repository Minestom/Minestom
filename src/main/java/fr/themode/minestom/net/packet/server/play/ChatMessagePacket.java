package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class ChatMessagePacket implements ServerPacket {

    private String jsonMessage;
    private Position position;

    public ChatMessagePacket(String jsonMessage, Position position) {
        this.jsonMessage = jsonMessage;
        this.position = position;
    }

    @Override
    public void write(PacketWriter writer) {
        writer.writeSizedString(jsonMessage);
        writer.writeByte((byte) position.ordinal());
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHAT_MESSAGE;
    }

    public enum Position {

        CHAT,
        SYSTEM_MESSAGE,
        GAME_INFO;
    }
}
