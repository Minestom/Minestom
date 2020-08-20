package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.UUID;

public class ChatMessagePacket implements ServerPacket {

    private String jsonMessage;
    private Position position;
    private UUID uuid;

    public ChatMessagePacket(String jsonMessage, Position position, UUID uuid) {
        this.jsonMessage = jsonMessage;
        this.position = position;
        this.uuid = uuid;
    }

    public ChatMessagePacket(String jsonMessage, Position position) {
        this(jsonMessage, position, new UUID(0, 0));
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeSizedString(jsonMessage);
        writer.writeByte((byte) position.ordinal());
        writer.writeUuid(uuid);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHAT_MESSAGE;
    }

    public enum Position {
        CHAT,
        SYSTEM_MESSAGE,
        GAME_INFO
    }
}
