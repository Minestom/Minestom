package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * Represents an outgoing chat message packet.
 */
public record PlayerChatMessagePacket(@NotNull Component signedContent, @Nullable Component unsignedContent,
                                      int type, @NotNull UUID uuid,
                                      @NotNull Component displayName, @Nullable Component teamDisplayName,
                                      @NotNull MessageSignature signature) implements ComponentHoldingServerPacket {
    public PlayerChatMessagePacket(BinaryReader reader) {
        this(reader.readComponent(), reader.readBoolean() ? reader.readComponent() : null,
                reader.readVarInt(), reader.readUuid(),
                reader.readComponent(), reader.readBoolean() ? reader.readComponent() : null,
                new MessageSignature(reader));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(signedContent);
        writer.writeBoolean(unsignedContent != null);
        if (unsignedContent != null) writer.writeComponent(unsignedContent);
        writer.writeVarInt(type);
        writer.writeUuid(uuid);
        writer.writeComponent(displayName);
        writer.writeBoolean(teamDisplayName != null);
        if (teamDisplayName != null) writer.writeComponent(teamDisplayName);
        writer.write(signature);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_CHAT;
    }

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(signedContent);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new PlayerChatMessagePacket(signedContent, unsignedContent, type,
                uuid, displayName, teamDisplayName, signature);
    }
}
