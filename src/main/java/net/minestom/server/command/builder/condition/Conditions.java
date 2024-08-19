package net.minestom.server.command.builder.condition;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;

/**
 * Common command conditions
 */
public class Conditions {
    /**
     * Will only execute if all command conditions succeed.
     */
    public static CommandCondition all(CommandCondition... conditions) {
        return (sender, commandString) -> {
            for (CommandCondition condition : conditions) {
                if (!condition.canUse(sender, commandString)) {
                    return false;
                }
            }

            return true;
        };
    }

    /**
     * Will execute if one or more command conditions succeed.
     */
    public static CommandCondition any(CommandCondition... conditions) {
        return (sender, commandString) -> {
            for (CommandCondition condition : conditions) {
                if (condition.canUse(sender, commandString)) {
                    return true;
                }
            }

            return false;
        };
    }

    /**
     * Will succeed if the command sender is a player.
     */
    public static boolean playerOnly(CommandSender sender, String commandString) {
        return sender instanceof Player;
    }

    /**
     * Will succeed if the command sender is the server console.
     */
    public static boolean consoleOnly(CommandSender sender, String commandString) {
        return sender instanceof ConsoleSender;
    }
}
