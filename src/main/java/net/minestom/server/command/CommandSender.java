package net.minestom.server.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.PermissionHandler;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Represents something which can send commands to the server.
 * <p>
 * Main implementations are {@link Player} and {@link ConsoleSender}.
 */
public interface CommandSender extends PermissionHandler, Audience, TagHandler {

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

    /**
     * Gets if the sender is a {@link Player}.
     * <p>
     * Consider using {@code instanceof} instead.
     *
     * @return true if 'this' is a player, false otherwise
     */
    @Deprecated
    default boolean isPlayer() {
        return false;
    }

    /**
     * Gets if the sender is a {@link ConsoleSender}.
     * <p>
     * Consider using {@code instanceof} instead.
     *
     * @return true if 'this' is the console, false otherwise
     */
    @Deprecated
    default boolean isConsole() {
        return false;
    }

    /**
     * Casts this object to a {@link Player}.
     * No checks are performed, {@link ClassCastException} can very much happen.
     *
     * @throws ClassCastException if 'this' is not a player
     * @see #isPlayer()
     */
    @Deprecated
    default Player asPlayer() {
        throw new ClassCastException("CommandSender is not a Player");
    }

    /**
     * Casts this object to a {@link ConsoleSender}.
     * No checks are performed, {@link ClassCastException} can very much happen.
     *
     * @throws ClassCastException if 'this' is not a console sender
     * @see #isConsole()
     */
    @Deprecated
    default ConsoleSender asConsole() {
        throw new ClassCastException("CommandSender is not the ConsoleSender");
    }
}
