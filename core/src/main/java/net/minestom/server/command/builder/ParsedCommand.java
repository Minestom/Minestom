package net.minestom.server.command.builder;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@link Command} ready to be executed (already parsed).
 */
public class ParsedCommand {

    // Command
    protected Command command;
    protected String commandString;

    // Command Executor
    protected CommandSyntax syntax;

    protected CommandExecutor executor;
    protected CommandContext context;

    // Argument Callback
    protected ArgumentCallback callback;
    protected ArgumentSyntaxException argumentSyntaxException;

    /**
     * Executes the command for the given source.
     * <p>
     * The command will not be executed if {@link Command#getCondition()}
     * is not validated.
     *
     * @param source the command source
     * @return the command data, null if none
     */
    @Nullable
    public CommandData execute(@NotNull CommandSender source) {
        // Global listener
        command.globalListener(source, context, commandString);
        // Command condition check
        final CommandCondition condition = command.getCondition();
        if (condition != null) {
            final boolean result = condition.canUse(source, commandString);
            if (!result)
                return null;
        }
        // Condition is respected
        if (executor != null) {
            // An executor has been found

            if (syntax != null) {
                // The executor is from a syntax
                final CommandCondition commandCondition = syntax.getCommandCondition();
                if (commandCondition == null || commandCondition.canUse(source, commandString)) {
                    context.retrieveDefaultValues(syntax.getDefaultValuesMap());
                    executor.apply(source, context);
                }
            } else {
                // The executor is probably the default one
                executor.apply(source, context);
            }
        } else if (callback != null && argumentSyntaxException != null) {
            // No syntax has been validated but the faulty argument with a callback has been found
            // Execute the faulty argument callback
            callback.apply(source, argumentSyntaxException);
        }

        if (context == null) {
            // Argument callbacks cannot return data
            return null;
        }

        return context.getReturnData();
    }

    @NotNull
    public static ParsedCommand withDefaultExecutor(@NotNull Command command, @NotNull String input) {
        ParsedCommand parsedCommand = new ParsedCommand();
        parsedCommand.command = command;
        parsedCommand.commandString = input;
        parsedCommand.executor = command.getDefaultExecutor();
        parsedCommand.context = new CommandContext(input);
        return parsedCommand;
    }

}
