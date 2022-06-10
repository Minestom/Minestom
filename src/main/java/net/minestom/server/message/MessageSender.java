package net.minestom.server.message;

import net.kyori.adventure.text.Component;
import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
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

    public static MessageSender forSigned(Player player) {
        return player.getTeam() == null ? forSigned(player.getUuid(), Objects.requireNonNullElse(player.getDisplayName(),
                Component.text(player.getUsername()))) : new MessageSender(player.getUuid(),
                Objects.requireNonNullElse(player.getDisplayName(), Component.text(player.getUsername())),
                player.getTeam().getTeamDisplayName());
    }

    public static MessageSender forUnsigned(Player player) {
        return player.getTeam() == null ? forUnsigned(Objects.requireNonNullElse(player.getDisplayName(),
                Component.text(player.getUsername()))) : forUnsigned(
                Objects.requireNonNullElse(player.getDisplayName(), Component.text(player.getUsername())),
                player.getTeam().getTeamDisplayName());
    }

    public boolean unsigned() {
        return MessageSignature.UNSIGNED_SENDER == uuid;
    }
}
