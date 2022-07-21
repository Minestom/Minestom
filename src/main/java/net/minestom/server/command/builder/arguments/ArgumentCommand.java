package net.minestom.server.command.builder.arguments;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.ParseResult;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

public class ArgumentCommand extends Argument<ParseResult> {

    public static final int INVALID_COMMAND_ERROR = 1;

    private boolean onlyCorrect;

    public ArgumentCommand(@NotNull String id) {
        super(id, true, true);
    }

    @NotNull
    @Override
    public ParseResult parse(@NotNull String input) throws ArgumentSyntaxException {
        if (!input.startsWith(getId())) throw new ArgumentSyntaxException("Incompatible", input, -1);

        final String commandString = input.substring(getId().length());
        final ParseResult parseResult = MinecraftServer.getCommandManager().parseCommand(commandString);

        if (onlyCorrect && !(parseResult instanceof ParseResult.KnownCommand.Valid))
            throw new ArgumentSyntaxException("Invalid command", input, INVALID_COMMAND_ERROR);

        return parseResult;
    }

    @Override
    public String parser() {
        return null;
    }

    public boolean isOnlyCorrect() {
        return onlyCorrect;
    }

    public ArgumentCommand setOnlyCorrect(boolean onlyCorrect) {
        this.onlyCorrect = onlyCorrect;
        return this;
    }

    @Override
    public String toString() {
        return String.format("Command<%s>", getId());
    }
}
