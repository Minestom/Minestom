package net.minestom.server.command;

public class ConsoleSender implements CommandSender {
    @Override
    public void sendMessage(String message) {
        System.out.println(message);
    }
}
