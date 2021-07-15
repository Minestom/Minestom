package net.minestom.server.command.builder.condition;

import net.minestom.server.command.CommandSender;

/**
 * Used to know if the {@link CommandSender} is allowed to run the command or a specific syntax.
 * @deprecated Renamed to {@link ExecuteCondition}
 */
@FunctionalInterface
@Deprecated
public interface CommandCondition extends ExecuteCondition {
}
