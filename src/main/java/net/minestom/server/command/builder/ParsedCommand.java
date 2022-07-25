package net.minestom.server.command.builder;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ExecutableCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@link Command} ready to be executed (already parsed).
 */
public class ParsedCommand {
    private final ExecutableCommand executableCommand;

    private ParsedCommand(ExecutableCommand executableCommand) {
        this.executableCommand = executableCommand;
    }

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
        return new ParsedCommand(executableCommand);
    }
}
