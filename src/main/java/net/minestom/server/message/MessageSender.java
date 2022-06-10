package net.minestom.server.message;

import net.kyori.adventure.text.Component;
import net.minestom.server.crypto.MessageSignature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record MessageSender(UUID uuid, @NotNull Component displayName, @Nullable Component teamName) {

    public static MessageSender forUnsigned(@NotNull Component displayName, @Nullable Component teamName) {
        return new MessageSender(MessageSignature.UNSIGNED_SENDER, displayName, teamName);
    }

    public static MessageSender forUnsigned(@NotNull Component displayName) {
        return new MessageSender(MessageSignature.UNSIGNED_SENDER, displayName, null);
    }

    public static MessageSender forSigned(@NotNull UUID uuid, @NotNull Component displayName) {
        return new MessageSender(uuid, displayName, null);
    }
}
