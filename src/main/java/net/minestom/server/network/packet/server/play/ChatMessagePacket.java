package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents an outgoing chat message packet. Do not use this to send messages above the
 * hotbar (the game info position) as it is preferred to use
 * {@link TitlePacket} due to <a href="https://bugs.mojang.com/browse/MC-119145">MC-119145</a>.
 */
public class ChatMessagePacket implements ServerPacket {
    private static final UUID NULL_UUID = new UUID(0, 0);

    public String message;
    public MessageType messageType;
    public UUID uuid;

    /**
     * @deprecated Use {@link #message}
     */
    public @Deprecated JsonMessage jsonMessage;
    /**
     * @deprecated Use {@link #messageType}
     */
    public @Deprecated Position position;

    @Deprecated
    public ChatMessagePacket(String jsonMessage, Position position, UUID uuid) {
        this(jsonMessage, position.asMessageType(), uuid);
    }

    @Deprecated
    public ChatMessagePacket(String jsonMessage, Position position) {
        this(jsonMessage, position, NULL_UUID);
    }

    /**
     * Constructs a new chat message packet with a zeroed UUID. To send formatted
     * messages please use the respective {@link Audience#sendMessage(Component)}
     * functions.
     *
     * @param jsonMessage the raw message payload
     * @param messageType the message type
     */
    public ChatMessagePacket(String jsonMessage, MessageType messageType) {
        this(jsonMessage, messageType, NULL_UUID);
    }

    /**
     * Constructs a new chat message packet. To send formatted messages please use the
     * respective {@link Audience#sendMessage(Component)} functions.
     *
     * @param message the raw message payload
     * @param messageType the message type
     * @param uuid the sender of the chat message
     */
    public ChatMessagePacket(String message, MessageType messageType, UUID uuid) {
        this.message = message;
        this.messageType = messageType;
        this.uuid = uuid;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(jsonMessage != null ? jsonMessage.toString() : message);
        writer.writeByte((byte) (position != null ? position.ordinal() : messageType == null ? 3 : messageType.ordinal()));
        writer.writeUuid(uuid);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHAT_MESSAGE;
    }

    /**
     * @deprecated Use {@link MessageType}
     */
    @Deprecated
    public enum Position {
        CHAT(MessageType.CHAT),
        SYSTEM_MESSAGE(MessageType.SYSTEM),
        GAME_INFO(null);

        private final MessageType messageType;

        Position(MessageType messageType) {
            this.messageType = messageType;
        }

        /**
         * Gets this position as an Adventure message type. Note this will return
         * {@code null} for {@link #GAME_INFO} as it is preferred to use
         * {@link TitlePacket} due to <a href="https://bugs.mojang.com/browse/MC-119145">MC-119145</a>.
         * @return the message type
         */
        public @Nullable MessageType asMessageType() {
            return this.messageType;
        }
    }
}
