package net.minestom.server.command.builder;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

/**
 * Callback executed when an error is found within the {@link Argument}.
 */
@FunctionalInterface
public interface ArgumentCallback {

    /**
     * Executed when an error is found.
     *
     * @param sender    the sender which executed the command
     * @param syntaxError the syntax error containing the message, input and error code related to the issue
     */
    void apply(@NotNull CommandSender sender, @NotNull Argument.Result.SyntaxError<?> syntaxError);
}
