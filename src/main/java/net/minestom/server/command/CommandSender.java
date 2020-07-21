package net.minestom.server.command;

import net.minestom.server.entity.Player;

/**
 * Represent something which can send commands to the server
 * <p>
 * Main implementations are {@link Player} and {@link ConsoleSender}
 */
public interface CommandSender {

    /**
     * Send a raw string message
     *
     * @param message the message to send
     */
    void sendMessage(String message);

    /**
     * Send multiple raw string messages
     *
     * @param messages the messages to send
     */
    default void sendMessage(String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    /**
     * Get if the sender is a player
     *
     * @return true if 'this' is a player, false otherwise
     */
    default boolean isPlayer() {
        return this instanceof Player;
    }

    /**
     * Get if the sender is the console
     *
     * @return true if 'this' is the console, false otherwise
     */
    default boolean isConsole() {
        return this instanceof ConsoleSender;
    }

}
