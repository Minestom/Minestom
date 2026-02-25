package net.minestom.server.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.Taggable;

/**
 * Represents something which can send commands to the server.
 * <p>
 * Main implementations are {@link Player} and {@link ConsoleSender}.
 */
public interface CommandSender extends Audience, Taggable, Identified {

    /**
     * Sends a raw string message.
     *
     * @param message the message to send
     */
    default void sendMessage(String message) {
        this.sendMessage(Component.text(message));
    }

    /**
     * Sends multiple raw string messages.
     *
     * @param messages the messages to send
     */
    default void sendMessage(String [] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }
}
