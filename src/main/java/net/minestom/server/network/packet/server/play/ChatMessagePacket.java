package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * Represents an outgoing chat message packet.
 */
public class ChatMessagePacket implements ComponentHoldingServerPacket {
    private static final UUID NULL_UUID = new UUID(0, 0);

    public Component message;
    public Position position;
    public UUID uuid;

    public ChatMessagePacket() {
        this(Component.empty(), Position.CHAT);
    }

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
    public void read(@NotNull BinaryReader reader) {
        message = reader.readComponent(Integer.MAX_VALUE);
        position = Position.values()[reader.readByte()];
        uuid = reader.readUuid();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHAT_MESSAGE;
    }

    @Override
    public @NotNull Collection<Component> components() {
        return Collections.singleton(message);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new ChatMessagePacket(operator.apply(message), position, uuid);
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
