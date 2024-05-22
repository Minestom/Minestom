package net.minestom.server.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.PermissionHandler;
import net.minestom.server.tag.Taggable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents something which can send commands to the server.
 * <p>
 * Main implementations are {@link Player} and {@link ConsoleSender}.
 */
public interface CommandSender extends PermissionHandler, Audience, Taggable, Identified {

    /**
     * Sends a raw string message.
     *
     * @param message the message to send
     */
    default void sendMessage(@NotNull String message) {
        this.sendMessage(Component.text(message));
    }

    /**
     * Sends multiple raw string messages.
     *
     * @param messages the messages to send
     */
    default void sendMessage(@NotNull String @NotNull [] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }
}
