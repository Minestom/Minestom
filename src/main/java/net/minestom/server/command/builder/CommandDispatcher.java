package net.minestom.server.command.builder;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.parser.CommandParser;
import net.minestom.server.command.builder.parser.CommandQueryResult;
import net.minestom.server.command.builder.parser.CommandSuggestionHolder;
import net.minestom.server.command.builder.parser.ValidSyntaxHolder;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Class responsible for parsing {@link Command}.
 */
public class CommandDispatcher {

    private final Map<String, Command> commandMap = new HashMap<>();
    private final Set<Command> commands = new HashSet<>();

    private final Cache<String, CommandResult> cache = Caffeine.newBuilder()
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
        CommandResult commandResult = parse(commandString);
        ParsedCommand parsedCommand = commandResult.parsedCommand;
        if (parsedCommand != null) {
            commandResult.commandData = parsedCommand.execute(source);
        }
        return commandResult;
    }

    /**
     * Parses the given command.
     *
     * @param commandString the command (containing the command name and the args if any)
     * @return the parsing result
     */
    public @NotNull CommandResult parse(@NotNull String commandString) {
        commandString = commandString.trim();
        // Verify if the result is cached
        {
            final CommandResult cachedResult = cache.getIfPresent(commandString);
            if (cachedResult != null) {
                return cachedResult;
            }
        }

        // Split space
        final String[] parts = commandString.split(StringUtils.SPACE);
        final String commandName = parts[0];

        final CommandQueryResult commandQueryResult = CommandParser.findCommand(this, commandString);
        // Check if the command exists
        if (commandQueryResult == null) {
            return CommandResult.of(CommandResult.Type.UNKNOWN, commandName);
        }
        CommandResult result = new CommandResult();
        result.input = commandString;
        // Find the used syntax and fill CommandResult#type and CommandResult#parsedCommand
        findParsedCommand( commandQueryResult, commandName, commandString, result);

        // Cache result
        this.cache.put(commandString, result);

        return result;
    }

    private @NotNull ParsedCommand findParsedCommand(@NotNull CommandQueryResult commandQueryResult,
                                                      @NotNull String commandName,
                                                      @NotNull String commandString,
                                                      @NotNull CommandResult result) {
        final Command command = commandQueryResult.command();
        String[] args = commandQueryResult.args();
        final boolean hasArgument = args.length > 0;

        final String input = commandName + StringUtils.SPACE + String.join(StringUtils.SPACE, args);

        ParsedCommand parsedCommand = new ParsedCommand();
        parsedCommand.parents = commandQueryResult.parents();
        parsedCommand.command = command;
        parsedCommand.commandString = commandString;

        // The default executor should be used if no argument is provided
        if (!hasArgument) {
            Optional<CommandSyntax> optionalSyntax = command.getSyntaxes()
                    .stream()
                    .filter(syntax -> syntax.getArguments().length == 0)
                    .findFirst();

            if (optionalSyntax.isPresent()) {
                // Empty syntax found
                final CommandSyntax syntax = optionalSyntax.get();
                parsedCommand.syntax = syntax;
                parsedCommand.executor = syntax.getExecutor();
                parsedCommand.context = new CommandContext(input);

                result.type = CommandResult.Type.SUCCESS;
                result.parsedCommand = parsedCommand;
                return parsedCommand;
            } else {
                // No empty syntax, use default executor if any
                final CommandExecutor defaultExecutor = command.getDefaultExecutor();
                if (defaultExecutor != null) {
                    parsedCommand.executor = defaultExecutor;
                    parsedCommand.context = new CommandContext(input);

                    result.type = CommandResult.Type.SUCCESS;
                    result.parsedCommand = parsedCommand;
                    return parsedCommand;
                }
            }
        }

        // SYNTAXES PARSING

        // All the registered syntaxes of the command
        final Collection<CommandSyntax> syntaxes = command.getSyntaxes();
        // Contains all the fully validated syntaxes (we later find the one with the most amount of arguments)
        List<ValidSyntaxHolder> validSyntaxes = new ArrayList<>(syntaxes.size());
        // Contains all the syntaxes that are not fully correct, used to later, retrieve the "most correct syntax"
        // Number of correct argument - The data about the failing argument
        Int2ObjectRBTreeMap<CommandSuggestionHolder> syntaxesSuggestions = new Int2ObjectRBTreeMap<>(Collections.reverseOrder());

        for (CommandSyntax syntax : syntaxes) {
            CommandParser.parse(syntax, syntax.getArguments(), args, commandString, validSyntaxes, syntaxesSuggestions);
        }

        // Check if there is at least one correct syntax
        if (!validSyntaxes.isEmpty()) {
            CommandContext context = new CommandContext(input);
            // Search the syntax with all perfect args
            final ValidSyntaxHolder finalValidSyntax = CommandParser.findMostCorrectSyntax(validSyntaxes, context);
            if (finalValidSyntax != null) {
                // A fully correct syntax has been found, use it
                final CommandSyntax syntax = finalValidSyntax.syntax();

                parsedCommand.syntax = syntax;
                parsedCommand.executor = syntax.getExecutor();
                parsedCommand.context = context;

                result.type = CommandResult.Type.SUCCESS;
                result.parsedCommand = parsedCommand;
                return parsedCommand;
            }
        }

        // No all-correct syntax, find the closest one to use the argument callback
        if (!syntaxesSuggestions.isEmpty()) {
            final int max = syntaxesSuggestions.firstIntKey(); // number of correct arguments in the most correct syntax
            final CommandSuggestionHolder suggestionHolder = syntaxesSuggestions.get(max);
            final CommandSyntax syntax = suggestionHolder.syntax();
            final ArgumentSyntaxException argumentSyntaxException = suggestionHolder.argumentSyntaxException();
            final int argIndex = suggestionHolder.argIndex();

            // Found the closest syntax with at least 1 correct argument
            final Argument<?> argument = syntax.getArguments()[argIndex];
            if (argument.hasErrorCallback() && argumentSyntaxException != null) {
                parsedCommand.callback = argument.getCallback();
                parsedCommand.argumentSyntaxException = argumentSyntaxException;

                result.type = CommandResult.Type.INVALID_SYNTAX;
                result.parsedCommand = parsedCommand;
                return parsedCommand;
            }
        }

        // No syntax found
        result.type = CommandResult.Type.INVALID_SYNTAX;
        result.parsedCommand = ParsedCommand.withDefaultExecutor(command, input);
        return result.parsedCommand;
    }
}
