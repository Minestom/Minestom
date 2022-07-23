package net.minestom.server.command.builder;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ExecutableCommand;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a {@link Command} ready to be executed (already parsed).
 */
public class ParsedCommand {

    protected ExecutableCommand executableCommand;

    // TODO remove all these fields
    // Command
    protected List<Command> parents;
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
    public @Nullable CommandData execute(@NotNull CommandSender source) {
        final ExecutableCommand.Result result = executableCommand.execute(source);
        return result.commandData();
    }

    public static @NotNull ParsedCommand fromExecutable(ExecutableCommand executableCommand) {
        ParsedCommand parsedCommand = new ParsedCommand();
        parsedCommand.executableCommand = executableCommand;
        return parsedCommand;
    }
}
