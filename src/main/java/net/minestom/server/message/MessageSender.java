package net.minestom.server.message;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record MessageSender(@NotNull Component displayName, @Nullable Component teamName) {

    public static MessageSender from(Player player) {
        return new MessageSender(Objects.requireNonNullElse(player.getDisplayName(),
                Component.text(player.getUsername())), player.getTeam() == null ? null :
                player.getTeam().getTeamDisplayName());
    }
}
