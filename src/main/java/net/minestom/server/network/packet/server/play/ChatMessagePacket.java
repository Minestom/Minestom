package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.message.ChatPosition;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * Represents an outgoing chat message packet.
 */
public class ChatMessagePacket implements ComponentHoldingServerPacket {
    private static final UUID NULL_UUID = new UUID(0, 0);

    public Component message;
    public ChatPosition position;
    public UUID uuid;

    public ChatMessagePacket(@NotNull Component message, @NotNull ChatPosition position, @Nullable UUID uuid) {
        this.message = message;
        this.position = position;
        this.uuid = Objects.requireNonNullElse(uuid, NULL_UUID);
    }

    public ChatMessagePacket() {
        this(Component.empty(), ChatPosition.SYSTEM_MESSAGE, NULL_UUID);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(message);
        writer.writeByte((byte) position.ordinal());
        writer.writeUuid(uuid);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        message = reader.readComponent();
        position = ChatPosition.fromPacketID(reader.readByte());
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
}
