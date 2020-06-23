package net.minestom.server.command;

import net.minestom.server.entity.Player;

public interface CommandSender {

    void sendMessage(String message);

    default void sendMessage(String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    default boolean isPlayer() {
        return this instanceof Player;
    }

    default boolean isConsole() {
        return this instanceof ConsoleSender;
    }

}
