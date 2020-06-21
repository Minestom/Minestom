package net.minestom.server.command;

public interface CommandSender {

    void sendMessage(String message);

    default void sendMessage(String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

}
