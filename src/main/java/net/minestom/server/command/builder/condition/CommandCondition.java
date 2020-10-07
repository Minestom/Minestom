package net.minestom.server.command.builder.condition;

import net.minestom.server.command.CommandSender;

/**
 * Used to know if the {@link CommandSender} is allowed to run the command
 */
public interface CommandCondition {
    boolean apply(CommandSender source);
}
