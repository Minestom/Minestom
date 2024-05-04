package net.minestom.server.command.builder;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandParser;
import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Class responsible for parsing {@link Command}.
 */
public class CommandDispatcher {
    private final CommandManager manager;

    private final Cache<String, CommandResult> cache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    public CommandDispatcher(CommandManager manager) {
        this.manager = manager;
    }

    public CommandDispatcher() {
        this(new CommandManager());
    }

    /**
     * Registers a command,
     * be aware that registering a command name or alias will override the previous entry.
     *
     * @param command the command to register
     */
    public void register(@NotNull Command command) {
        manager.register(command);
    }

    public void unregister(@NotNull Command command) {
        manager.unregister(command);
    }

    public @NotNull Set<Command> getCommands() {
        return manager.getCommands();
    }

    /**
     * Gets the command class associated with the name.
     *
     * @param commandName the command name
     * @return the {@link Command} associated with the name, null if not any
     */
    public @Nullable Command findCommand(@NotNull String commandName) {
        return manager.getCommand(commandName);
    }

    /**
     * Checks if the command exists, and execute it.
     *
     * @param source        the command source
     * @param commandString the command with the argument(s)
     * @return the command result
     */
    public @NotNull CommandResult execute(@NotNull CommandSender source, @NotNull String commandString) {
        return manager.execute(source, commandString);
    }

    /**
     * Parses the given command.
     *
     * @param commandString the command (containing the command name and the args if any)
     * @return the parsing result
     */
    public @NotNull CommandResult parse(@NotNull CommandSender sender, @NotNull String commandString) {
        final net.minestom.server.command.CommandParser.Result test = manager.parseCommand(sender, commandString);
        return resultConverter(test, commandString);
    }

    private static CommandResult resultConverter(net.minestom.server.command.CommandParser.Result parseResult, String input) {
        return CommandResult.of(switch (parseResult) {
            case CommandParser.Result.UnknownCommand unknownCommand -> CommandResult.Type.UNKNOWN;
            case CommandParser.Result.KnownCommand.Valid valid -> CommandResult.Type.SUCCESS;
            case CommandParser.Result.KnownCommand.Invalid invalid -> CommandResult.Type.INVALID_SYNTAX;
            case null, default -> throw new IllegalStateException("Unknown CommandParser.Result type");
        }, input, ParsedCommand.fromExecutable(parseResult.executable()), null);
    }
}
