package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents an outgoing chat message packet.
 */
public class ChatMessagePacket implements ServerPacket {
    private static final UUID NULL_UUID = new UUID(0, 0);

    public Component message;
    public Position position;
    public UUID uuid;

    public ChatMessagePacket(Component message, Position position, UUID uuid) {
        this.message = message;
        this.position = position;
        this.uuid = uuid;
    }

    public ChatMessagePacket(Component message, Position position) {
        this(message, position, NULL_UUID);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(message);
        writer.writeByte((byte) position.ordinal());
        writer.writeUuid(uuid);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHAT_MESSAGE;
    }

    public enum Position {
        CHAT(MessageType.CHAT),
        SYSTEM_MESSAGE(MessageType.SYSTEM),
        GAME_INFO(null);

        private final MessageType messageType;

        Position(MessageType messageType) {
            this.messageType = messageType;
        }

        /**
         * Gets the Adventure message type from this position. Note that there is no
         * message type for {@link #GAME_INFO}, as Adventure uses the title methods for this.
         *
         * @return the message type, if any
         */
        public @Nullable MessageType getMessageType() {
            return this.messageType;
        }

        /**
         * Gets a position from an Adventure message type.
         *
         * @param messageType the message type
         *
         * @return the position
         */
        public static @NotNull Position fromMessageType(@NotNull MessageType messageType) {
            switch (messageType) {
                case CHAT: return CHAT;
                case SYSTEM: return SYSTEM_MESSAGE;
            }
            throw new IllegalArgumentException("Cannot get position from message type!");
        }
    }
}
