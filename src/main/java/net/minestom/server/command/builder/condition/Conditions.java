package net.minestom.server.command.builder.condition;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

/**
 * Common command conditions
 */
public class Conditions {
    /**
     * Will only execute if all command conditions succeed.
     */
    public static CommandCondition all(@NotNull CommandCondition @NotNull... conditions) {
        Check.notNull(conditions, "conditions cannot be null");
        for (CommandCondition condition : conditions) {
            Check.notNull(condition, "condition cannot be null");
        }
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
    public static CommandCondition any(@NotNull CommandCondition @NotNull... conditions) {
        Check.notNull(conditions, "conditions cannot be null");
        for (CommandCondition condition : conditions) {
            Check.notNull(condition, "condition cannot be null");
        }
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

    /**
     * Inverts the result of the given condition.
     */
    public static CommandCondition not(@NotNull CommandCondition condition) {
        Check.notNull(condition, "condition cannot be null");
        return (sender, commandString) -> !condition.canUse(sender, commandString);
    }
}
