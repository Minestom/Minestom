package net.minestom.server.command.builder.condition;


import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;

/**
 * Common command conditions
 */
public class Conditions {
    public static boolean playerOnly(CommandSender sender, String commandString) {
        if (!sender.isPlayer()) {
            sender.sendMessage(Component.text("The command is only available for players"));
            return false;
        }
        return true;
    }
    public static boolean consoleOnly(CommandSender sender, String commandString) {
        if (!sender.isConsole()) {
            sender.sendMessage(Component.text("The command is only available form the console"));
            return false;
        }
        return true;
    }
}
