package net.minestom.server.command.builder;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.command.CommandParser;
import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Class responsible for parsing {@link Command}.
 */
public class CommandDispatcher { //Todo maybe merge with manager?

    private final Map<String, Command> commandMap = new HashMap<>();
    private final Set<Command> commands = new HashSet<>();
    private final Cache<String, CommandParser.Result> cache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    /**
     * Registers a command,
     * be aware that registering a command name or alias will override the previous entry.
     *
     * @param command the command to register
     */
    public void register(@NotNull Command command) {
        this.commandMap.put(command.getName().toLowerCase(), command);

        // Register aliases
        final String[] aliases = command.getAliases();
        if (aliases != null) {
            for (String alias : aliases) {
                this.commandMap.put(alias.toLowerCase(), command);
            }
        }

        this.commands.add(command);
    }

    public void unregister(@NotNull Command command) {
        this.commandMap.remove(command.getName().toLowerCase());

        final String[] aliases = command.getAliases();
        if (aliases != null) {
            for (String alias : aliases) {
                this.commandMap.remove(alias.toLowerCase());
            }
        }

        this.commands.remove(command);
        // Clear cache
        this.cache.invalidateAll();
    }

    public @NotNull Set<Command> getCommands() {
        return Collections.unmodifiableSet(commands);
    }

    /**
     * Gets the command class associated with the name.
     *
     * @param commandName the command name
     * @return the {@link Command} associated with the name, null if not any
     */
    public @Nullable Command findCommand(@NotNull String commandName) {
        commandName = commandName.toLowerCase();
        return commandMap.getOrDefault(commandName, null);
    }

    /**
     * Checks if the command exists, and execute it.
     *
     * @param source        the command source
     * @param commandString the command with the argument(s)
     * @return the command result
     */
    public @NotNull CommandResult execute(@NotNull CommandSender source, @NotNull String commandString) {
        final CommandParser.Result result = parse(commandString);
        final CommandContext context = new CommandContext(commandString).setArgs(result.arguments());
        return result.execute(source, context);
    }

    public @NotNull CommandParser.Result parse(@NotNull String commandString) {
        return null;
    }
}
