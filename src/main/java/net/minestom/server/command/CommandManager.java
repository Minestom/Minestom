package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandDispatcher;
import net.minestom.server.command.builder.CommandResult;
import net.minestom.server.command.builder.ParsedCommand;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.callback.CommandCallback;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Manager used to register {@link Command commands}.
 * <p>
 * It is also possible to simulate a command using {@link #execute(CommandSender, String)}.
 */
public final class CommandManager {

    public static final String COMMAND_PREFIX = "/";

    private final ServerSender serverSender = new ServerSender();
    private final ConsoleSender consoleSender = new ConsoleSender();
    private final CommandParser parser = CommandParser.parser();
    private final CommandDispatcher dispatcher = new CommandDispatcher(this);
    private final Map<String, Command> commandMap = new HashMap<>();
    private final Set<Command> commands = new HashSet<>();

    private CommandCallback unknownCommandCallback;

    public CommandManager() {
    }

    /**
     * Registers a {@link Command}.
     *
     * @param command the command to register
     * @throws IllegalStateException if a command with the same name already exists
     */
    public synchronized void register(@NotNull Command command) {
        Check.stateCondition(commandExists(command.getName()),
                "A command with the name " + command.getName() + " is already registered!");
        if (command.getAliases() != null) {
            for (String alias : command.getAliases()) {
                Check.stateCondition(commandExists(alias),
                        "A command with the name " + alias + " is already registered!");
            }
        }
        commands.add(command);
        for (String name : command.getNames()) {
            commandMap.put(name, command);
        }
    }

    /**
     * Removes a command from the currently registered commands.
     * Does nothing if the command was not registered before
     *
     * @param command the command to remove
     */
    public void unregister(@NotNull Command command) {
        commands.remove(command);
        for (String name : command.getNames()) {
            commandMap.remove(name);
        }
    }

    /**
     * Gets the {@link Command} registered by {@link #register(Command)}.
     *
     * @param commandName the command name
     * @return the command associated with the name, null if not any
     */
    public @Nullable Command getCommand(@NotNull String commandName) {
        return commandMap.get(commandName.toLowerCase(Locale.ROOT));
    }

    /**
     * Gets if a command with the name {@code commandName} already exists or not.
     *
     * @param commandName the command name to check
     * @return true if the command does exist
     */
    public boolean commandExists(@NotNull String commandName) {
        return getCommand(commandName) != null;
    }

    /**
     * Executes a command for a {@link CommandSender}.
     *
     * @param sender  the sender of the command
     * @param command the raw command string (without the command prefix)
     * @return the execution result
     */
    public @NotNull CommandResult execute(@NotNull CommandSender sender, @NotNull String command) {
        command = command.trim();
        // Command event
        if (sender instanceof Player player) {
            PlayerCommandEvent playerCommandEvent = new PlayerCommandEvent(player, command);
            EventDispatcher.call(playerCommandEvent);
            if (playerCommandEvent.isCancelled())
                return CommandResult.of(CommandResult.Type.CANCELLED, command);
            command = playerCommandEvent.getCommand();
        }
        // Process the command
        final CommandParser.Result parsedCommand = parseCommand(command);
        final ExecutableCommand executable = parsedCommand.executable();
        final ExecutableCommand.Result executeResult = executable.execute(sender);
        final CommandResult result = resultConverter(executable, executeResult, command);
        if (result.getType() == CommandResult.Type.UNKNOWN) {
            if (unknownCommandCallback != null) {
                this.unknownCommandCallback.apply(sender, command);
            }
        }
        return result;
    }

    /**
     * Executes the command using a {@link ServerSender}. This can be used
     * to run a silent command (nothing is printed to console).
     *
     * @see #execute(CommandSender, String)
     */
    public @NotNull CommandResult executeServerCommand(@NotNull String command) {
        return execute(serverSender, command);
    }

    public @NotNull CommandDispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * Gets the callback executed once an unknown command is run.
     *
     * @return the unknown command callback, null if not any
     */
    public @Nullable CommandCallback getUnknownCommandCallback() {
        return unknownCommandCallback;
    }

    /**
     * Sets the callback executed once an unknown command is run.
     *
     * @param unknownCommandCallback the new unknown command callback,
     *                               setting it to null mean that nothing will be executed
     */
    public void setUnknownCommandCallback(@Nullable CommandCallback unknownCommandCallback) {
        this.unknownCommandCallback = unknownCommandCallback;
    }

    /**
     * Gets the {@link ConsoleSender} (which is used as a {@link CommandSender}).
     *
     * @return the {@link ConsoleSender}
     */
    public @NotNull ConsoleSender getConsoleSender() {
        return consoleSender;
    }

    /**
     * Gets the {@link DeclareCommandsPacket} for a specific player.
     * <p>
     * Can be used to update a player auto-completion list.
     *
     * @param player the player to get the commands packet
     * @return the {@link DeclareCommandsPacket} for {@code player}
     */
    public @NotNull DeclareCommandsPacket createDeclareCommandsPacket(@NotNull Player player) {
        return GraphConverter.createPacket(getGraph(), player);
    }

    public @NotNull Set<@NotNull Command> getCommands() {
        return Collections.unmodifiableSet(commands);
    }

    /**
     * Parses the command based on the registered commands
     *
     * @param input commands string without prefix
     * @return the parsing result
     */
    public CommandParser.Result parseCommand(String input) {
        return parser.parse(getGraph(), input);
    }

    private Graph getGraph() {
        //todo cache
        return Graph.merge(commands);
    }

    private static CommandResult resultConverter(ExecutableCommand executable,
                                                 ExecutableCommand.Result newResult,
                                                 String input) {
        return CommandResult.of(switch (newResult.type()) {
            case SUCCESS -> CommandResult.Type.SUCCESS;
            case CANCELLED, PRECONDITION_FAILED, EXECUTOR_EXCEPTION -> CommandResult.Type.CANCELLED;
            case INVALID_SYNTAX -> CommandResult.Type.INVALID_SYNTAX;
            case UNKNOWN -> CommandResult.Type.UNKNOWN;
        }, input, ParsedCommand.fromExecutable(executable), newResult.commandData());
    }
}
