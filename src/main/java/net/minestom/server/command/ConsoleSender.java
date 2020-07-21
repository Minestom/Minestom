package net.minestom.server.command;

/**
 * Represent the console when sending a command to the server
 */
public class ConsoleSender implements CommandSender {

    @Override
    public void sendMessage(String message) {
        System.out.println(message);
    }
}
