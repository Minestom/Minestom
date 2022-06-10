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
import java.util.UUID;
import java.util.function.UnaryOperator;

// TODO Check the need of ComponentHolding
/**
 * Represents an outgoing chat message packet.
 */
public record PlayerChatMessagePacket(@NotNull Component signedContent, @Nullable Component unsignedContent,
                                      @NotNull ChatPosition position, @NotNull UUID uuid,
                                      @NotNull Component displayName, @Nullable Component teamDisplayName,
                                      long timestamp, long salt,
                                      byte[] signature) implements ComponentHoldingServerPacket {
    public PlayerChatMessagePacket(BinaryReader reader) {
        this(reader.readComponent(), reader.readBoolean() ? reader.readComponent() : null,
                ChatPosition.fromPacketID(reader.readVarInt()), reader.readUuid(),
                reader.readComponent(), reader.readBoolean() ? reader.readComponent() : null,
                reader.readLong(), reader.readLong(), reader.readByteArray());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(signedContent);
        writer.writeBoolean(unsignedContent != null);
        if (unsignedContent != null) writer.writeComponent(unsignedContent);
        writer.writeVarInt((byte) position.ordinal());
        writer.writeUuid(uuid);
        writer.writeComponent(displayName);
        writer.writeBoolean(teamDisplayName != null);
        if (teamDisplayName != null) writer.writeComponent(teamDisplayName);
        writer.writeLong(timestamp);
        writer.writeLong(salt);
        writer.writeByteArray(signature);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_CHAT;
    }

    @Override
    public @NotNull Collection<Component> components() {
        return Collections.singleton(signedContent);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new PlayerChatMessagePacket(signedContent, unsignedContent, position,
                uuid, displayName, teamDisplayName, timestamp, salt, signature);
    }
}
