package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.message.ChatPosition;
import net.minestom.server.message.MessageSender;
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
public record PlayerChatMessagePacket(@NotNull Component signedContent, @Nullable Component unsignedContent,
                                      int type, @NotNull UUID uuid,
                                      @NotNull Component displayName, @Nullable Component teamDisplayName,
                                      @NotNull MessageSignature signature) implements ComponentHoldingServerPacket {
    public PlayerChatMessagePacket(BinaryReader reader) {
        this(reader.readComponent(), reader.readNullableComponent(), reader.readVarInt(), reader.readUuid(),
                reader.readComponent(), reader.readNullableComponent(), new MessageSignature(reader));
    }

    public static PlayerChatMessagePacket unsigned(@NotNull Component message, ChatPosition type, @NotNull MessageSender sender) {
        return new PlayerChatMessagePacket(message, null, type.getID(), MessageSignature.UNSIGNED_SENDER,
                sender.displayName(), sender.teamName(), MessageSignature.UNSIGNED);
    }

    public static PlayerChatMessagePacket signed(@NotNull Component message, ChatPosition type, @NotNull MessageSender sender,
                                                 @NotNull MessageSignature signature) {
        return new PlayerChatMessagePacket(message, null, type.getID(), sender.uuid(),
                sender.displayName(), sender.teamName(), signature);
    }

    public static PlayerChatMessagePacket signedWithUnsignedContent(@NotNull Component message,
                                                                    @NotNull Component unsignedContent,
                                                                    ChatPosition type, @NotNull MessageSender sender,
                                                                    @NotNull MessageSignature signature) {
        return new PlayerChatMessagePacket(message, unsignedContent, type.getID(), sender.uuid(),
                sender.displayName(), sender.teamName(), signature);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(signedContent);
        writer.writeNullableComponent(unsignedContent);
        writer.writeVarInt(type);
        writer.writeUuid(uuid);
        writer.writeComponent(displayName);
        writer.writeNullableComponent(teamDisplayName);
        writer.write(signature);
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
        return new PlayerChatMessagePacket(signedContent, unsignedContent, type,
                uuid, displayName, teamDisplayName, signature);
    }
}
