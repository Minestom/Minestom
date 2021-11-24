package net.minestom.server.command.builder.condition;


import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;

/**
 * Common command conditions
 */
public class Conditions {
    public static boolean playerOnly(CommandSender sender, String commandString) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("The command is only available for players"));
            return false;
        }
        return true;
    }

    public static boolean consoleOnly(CommandSender sender, String commandString) {
        if (!(sender instanceof ConsoleSender)) {
            sender.sendMessage(Component.text("The command is only available form the console"));
            return false;
        }
        return true;
    }
}
