package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.message.ChatPosition;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * Represents an outgoing chat message packet.
 */
public record ChatMessagePacket(@NotNull Component message, @NotNull ChatPosition position,
                                @NotNull UUID uuid) implements ComponentHoldingServerPacket {
    public ChatMessagePacket(BinaryReader reader) {
        this(reader.readComponent(), ChatPosition.fromPacketID(reader.readByte()),
                reader.readUuid());
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

    @Override
    public @NotNull Collection<Component> components() {
        return Collections.singleton(message);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new ChatMessagePacket(operator.apply(message), position, uuid);
    }
}
