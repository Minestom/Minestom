package net.minestom.server.command.builder;

import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandParser;
import net.minestom.server.command.CommandSender;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Class responsible for parsing {@link Command}.
 */
public class CommandDispatcher {
    private final CommandManager manager;

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
    public void register(Command command) {
        manager.register(command);
    }

    public void unregister(Command command) {
        manager.unregister(command);
    }

    public Set<Command> getCommands() {
        return manager.getCommands();
    }

    /**
     * Gets the command class associated with the name.
     *
     * @param commandName the command name
     * @return the {@link Command} associated with the name, null if not any
     */
    public @Nullable Command findCommand(String commandName) {
        return manager.getCommand(commandName);
    }

    /**
     * Checks if the command exists, and execute it.
     *
     * @param source        the command source
     * @param commandString the command with the argument(s)
     * @return the command result
     */
    public CommandResult execute(CommandSender source, String commandString) {
        return manager.execute(source, commandString);
    }

    /**
     * Parses the given command.
     *
     * @param commandString the command (containing the command name and the args if any)
     * @return the parsing result
     */
    public CommandResult parse(CommandSender sender, String commandString) {
        final net.minestom.server.command.CommandParser.Result test = manager.parseCommand(sender, commandString);
        return resultConverter(test, commandString);
    }

    private static CommandResult resultConverter(net.minestom.server.command.CommandParser.Result parseResult, String input) {
        CommandResult.Type type;
        if (parseResult instanceof CommandParser.Result.UnknownCommand) {
            type = CommandResult.Type.UNKNOWN;
        } else if (parseResult instanceof CommandParser.Result.KnownCommand.Valid) {
            type = CommandResult.Type.SUCCESS;
        } else if (parseResult instanceof CommandParser.Result.KnownCommand.Invalid) {
            type = CommandResult.Type.INVALID_SYNTAX;
        } else {
            throw new IllegalStateException("Unknown CommandParser.Result type");
        }
        return CommandResult.of(type, input, ParsedCommand.fromExecutable(parseResult.executable()), null);
    }
}
