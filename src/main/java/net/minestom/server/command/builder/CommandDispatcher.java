package net.minestom.server.command.builder;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The class responsible for executing commands. This should generally be wrapped by a CommandManager to manage things
 * like synchronization, extra parsing, more features, etc.
 */
public class CommandDispatcher {

    public final static Logger LOGGER = LoggerFactory.getLogger(CommandDispatcher.class);

    private final Object2ObjectOpenHashMap<String, Command> commandNamesMap = new Object2ObjectOpenHashMap<>();
    private final Set<Command> commands = new HashSet<>();

    private final Set<Command> commandsView = Collections.unmodifiableSet(commands);

    private final Cache<String, CommandResult> cache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    /**
     * Registers the provided command in this dispatcher. Any names will overwrite the names of other commands.
     */
    public void register(@NotNull Command command) {
        if (!commands.add(command)) {
            LOGGER.warn("The command \"" + command.getName() + "\" is already registered in this dispatcher!");
            return;
        }
        for (String name : command.getFormattedNames()) {
            Command previousValue = commandNamesMap.put(name, command);
            if (previousValue != null) {
                cache.invalidateAll();
                LOGGER.warn("The command \"" + command.getName() + "\" overwrites the name \"" + name + "\" which was" +
                        " registered by another command \"" + previousValue.getName() + "\".");
            }
        }
    }

    /**
     * Unregisters the provided command from this dispatcher.
     */
    public void unregister(@NotNull Command command) {
        if (!commands.remove(command)) {
            LOGGER.warn("The command \"" + command.getName() + "\" is not registered in this dispatcher!");
            return;
        }
        for (String name : command.getFormattedNames()) {
            commandNamesMap.remove(name, command);
        }
        cache.invalidateAll();
    }

    /**
     * @return the set of commands that are registered in this dispatcher
     */
    public @NotNull Set<Command> getCommands() {
        return commandsView;
    }

    /**
     * Finds the command that has a name contained within the start of this reader that is the longest out of all
     * registered names. For example, this method would choose the alias "test command" instead of "test" in all cases.
     * This method reads the name from the reader, and it requires that there is either no text after the name or there
     * is a valid whitespace character after it. However, if there is whitespace, this method does not read it.
     */
    public @Nullable Command findCommand(@NotNull StringReader reader) {
        int longestLength = -1;
        Command currentLongest = null;
        // Directly iterate instead of using an enhanced for loop so we can use the fastIterator method to avoid
        // creating excessive objects
        var iterator = commandNamesMap.object2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getKey().length() > longestLength &&
                    reader.canRead(entry.getKey(), true) &&
                    (!reader.canRead(entry.getKey().length() + 1) ||
                            StringReader.isValidWhitespace(reader.peek(entry.getKey().length())))
            ) {
                longestLength = entry.getKey().length();
                currentLongest = entry.getValue();
            }
        }

        if (longestLength >= 0) {
            reader.skip(longestLength);
        }

        return currentLongest;
    }

    /**
     * Find any command that has been registered to this dispatcher and has the provided input as one of its aliases.
     */
    public @Nullable Command findCommand(@NotNull String input) {
        return commandNamesMap.get(input);
    }

    public @NotNull CommandResult execute(@NotNull CommandOrigin origin, @NotNull StringReader reader) {
        CommandResult result = parse(reader);
        if (result.parsedCommand() != null) {
            result.parsedCommand().execute(origin);
        }
        return result;
    }

    public @Nullable Suggestion tabComplete(@NotNull CommandOrigin origin, @NotNull StringReader reader) {
        Command command = findCommand(reader);
        if (command == null) {
            return null;
        }
        List<Command> parents = new ArrayList<>();
        command = roamSubcommands(command, reader, parents);
        if (!reader.canRead()) {
            return null;
        }

        List<CommandSyntax> syntaxes = command.getSyntaxes();
        if (syntaxes.isEmpty()) {
            return null;
        }

        int start = reader.position();
        ParsedCommand primaryContext = new ParsedCommand();
        primaryContext.setCommand(command).setMessage(reader.all()).setReaderPosition(-1).setArgumentNumber(-1)
                .setStartingPosition(start).setParents(parents);
        final Map<String, Object> temporaryArgumentMap = new HashMap<>();

        SyntaxLoop:
        for (CommandSyntax syntax : syntaxes) {
            reader.position(start);
            temporaryArgumentMap.clear();
            int tempPos;
            int lastStart = -1;
            for (int i = 0; i < syntax.getArguments().size(); i++) {
                Argument<?> argument = syntax.getArguments().get(i);
                tempPos = reader.position();
                reader.skipWhitespace();
                int tempStart = reader.position();
                if (tempPos == reader.position()) {
                    if (i > 0) {
                        // switch to previous
                        boolean shouldOverride = tempPos > primaryContext.getReaderPosition();
                        if (!shouldOverride && tempPos == primaryContext.getReaderPosition() && primaryContext.getSyntax() != null) {
                            CommandSyntax commandSyntax = primaryContext.getSyntax();
                            if (primaryContext.getArgumentNumber() >= 0 && primaryContext.getArgumentNumber() < commandSyntax.getArguments().size()) {
                                Argument<?> arg = commandSyntax.getArguments().get(primaryContext.getArgumentNumber());
                                if (!arg.hasSuggestion()) {
                                    shouldOverride = true;
                                }
                            }
                        }

                        // Set to start of last argument
                        if (shouldOverride) {
                            primaryContext.setSyntax(syntax).setArgumentMap(new HashMap<>(temporaryArgumentMap))
                                    .setReaderPosition(lastStart).setArgumentNumber(i - 1).setException(null).setSuccess(false);
                        }
                    }
                    continue SyntaxLoop;
                }
                tempPos = reader.position();
                if (!reader.canRead()) {
                    boolean shouldOverride = tempPos > primaryContext.getReaderPosition();
                    if (!shouldOverride && tempPos == primaryContext.getReaderPosition() && primaryContext.getSyntax() != null) {
                        CommandSyntax commandSyntax = primaryContext.getSyntax();
                        if (primaryContext.getArgumentNumber() >= 0 && primaryContext.getArgumentNumber() < commandSyntax.getArguments().size()) {
                            Argument<?> arg = commandSyntax.getArguments().get(primaryContext.getArgumentNumber());
                            if (!arg.hasSuggestion()) {
                                shouldOverride = true;
                            }
                        }
                    }

                    if (shouldOverride) {
                        primaryContext.setSyntax(syntax).setArgumentMap(new HashMap<>(temporaryArgumentMap))
                                .setReaderPosition(tempPos).setArgumentNumber(i).setException(null).setSuccess(false);
                    }
                    continue SyntaxLoop;
                }
                try {
                    temporaryArgumentMap.put(argument.getId(), argument.parse(reader));
                } catch (CommandException exception) {
                    boolean shouldOverride = tempPos > primaryContext.getReaderPosition();
                    if (!shouldOverride && tempPos == primaryContext.getReaderPosition() && primaryContext.getSyntax() != null) {
                        CommandSyntax commandSyntax = primaryContext.getSyntax();
                        if (primaryContext.getArgumentNumber() >= 0 && primaryContext.getArgumentNumber() < commandSyntax.getArguments().size()) {
                            Argument<?> arg = commandSyntax.getArguments().get(primaryContext.getArgumentNumber());
                            if (!arg.hasSuggestion()) {
                                shouldOverride = true;
                            }
                        }
                    }

                    if (shouldOverride) {
                        primaryContext.setSyntax(syntax).setArgumentMap(new HashMap<>(temporaryArgumentMap))
                                .setReaderPosition(tempPos).setArgumentNumber(i).setException(exception).setSuccess(false);
                    }
                    continue SyntaxLoop;
                }

                lastStart = tempStart;
            }
        }

        if (primaryContext.getSyntax() != null) {
            CommandSyntax syntax = primaryContext.getSyntax();
            List<Argument<?>> arguments = syntax.getArguments();
            if (primaryContext.getArgumentNumber() >= 0 && primaryContext.getArgumentNumber() < arguments.size()) {
                Argument<?> argument = arguments.get(primaryContext.getArgumentNumber());

                if (argument.getSuggestionCallback() != null) {
                    SuggestionCallback callback = argument.getSuggestionCallback();

                    Suggestion suggestion = new Suggestion(reader.all(), primaryContext.getReaderPosition(), reader.length());
                    CommandContext context = primaryContext.toContext();

                    callback.apply(origin, context, suggestion);

                    return suggestion;
                }
            }
        }
        return null;
    }

    public @NotNull CommandResult parse(@NotNull StringReader reader) {
        final CommandResult result = cache.getIfPresent(reader.unread());
        if (result != null) {
            return result;
        }

        int position = reader.position();
        Command command = findCommand(reader);

        if (command == null) {
            return new CommandResult(CommandResult.Type.UNKNOWN_COMMAND, reader.all(), position);
        }

        List<Command> parents = new ArrayList<>();
        command = roamSubcommands(command, reader, parents);

        ParsedCommand parsedCommand = parseSyntaxes(command.getSyntaxes(), reader).setCommand(command).setParents(parents);

        if (parsedCommand.getSyntax() != null && parsedCommand.getSyntax().getDefaultValuesMap() != null) {
            if (parsedCommand.getArgumentMap() == null) {
                parsedCommand.setArgumentMap(new HashMap<>());
            }
            for (var entry : parsedCommand.getSyntax().getDefaultValuesMap().entrySet()) {
                parsedCommand.getArgumentMap().computeIfAbsent(entry.getKey(), t -> entry.getValue().get());
            }
        }

        CommandResult commandResult = new CommandResult(parsedCommand.isSuccess() ? CommandResult.Type.SUCCESS : CommandResult.Type.FAILURE, reader.all(), position, parsedCommand);

        for (CommandSyntax syntax : command.getSyntaxes()) {
            for (Argument<?> argument : syntax.getArguments()) {
                if (!argument.shouldCache()) {
                    return commandResult;
                }
            }
        }
        cache.put(reader.all().substring(position), commandResult);
        return commandResult;
    }

    private @NotNull ParsedCommand parseSyntaxes(@NotNull List<CommandSyntax> syntaxes, @NotNull StringReader reader) {

        int start = reader.position();
        ParsedCommand primaryContext = new ParsedCommand();
        primaryContext.setMessage(reader.all()).setReaderPosition(-1).setArgumentNumber(-1).setStartingPosition(start);

        final Map<String, Object> temporaryArgumentMap = new HashMap<>();

        // Unexpected behavior will occur if the following loop runs when there are no syntaxes.
        if (syntaxes.isEmpty()) {
            return primaryContext.setSyntax(null).setArgumentMap(null).setArgumentNumber(-1)
                    .setException(CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(reader.all(), start))
                    .setSuccess(false);
        }

        if (!reader.canRead()) {
            for (CommandSyntax syntax : syntaxes) {
                if (syntax.getArguments().isEmpty()) {
                    return primaryContext.setSyntax(syntax).setArgumentMap(new HashMap<>()).setArgumentNumber(-1).setSuccess(true);
                }
            }
            return primaryContext.setSyntax(null).setArgumentMap(null).setArgumentNumber(-1)
                    .setException(CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(reader.all(), start))
                    .setSuccess(false);
        }

        SyntaxLoop:
        for (CommandSyntax syntax : syntaxes) {
            // Reset the reader and temporary argument map so we don't have to create a new one for each syntax
            reader.position(start);
            temporaryArgumentMap.clear();

            for (int i = 0; i < syntax.getArguments().size(); i++) {
                Argument<?> argument = syntax.getArguments().get(i);
                try {
                    reader.assureWhitespace();
                    temporaryArgumentMap.put(argument.getId(), argument.parse(reader));
                } catch (CommandException exception) {
                    if (exception.getPosition() > primaryContext.getReaderPosition()) {
                        primaryContext.setSyntax(syntax).setArgumentMap(new HashMap<>(temporaryArgumentMap))
                                .setReaderPosition(exception.getPosition()).setArgumentNumber(i).setException(exception)
                                .setSuccess(false);
                    } else if (exception.getPosition() == primaryContext.getReaderPosition()) {
                        primaryContext.setSyntax(null).setArgumentMap(null).setArgumentNumber(-1)
                                .setException(CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(primaryContext.getMessage(), primaryContext.getReaderPosition()))
                                .setSuccess(false);
                    }
                    continue SyntaxLoop;
                }
            }

            if (reader.canRead()) {
                int store = reader.position();
                reader.skipWhitespace();
                if (reader.position() > primaryContext.getReaderPosition()) {
                    // Use "Incorrect argument for command" if there isn't any whitespace, otherwise complain about there not being whitespace
                    CommandException exception = (store == reader.position()) ?
                            CommandException.COMMAND_EXPECTED_SEPARATOR.generateException(reader.all(), store) :
                            CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(primaryContext.getMessage(), reader.position());
                    primaryContext.setSyntax(syntax).setArgumentMap(new HashMap<>(temporaryArgumentMap))
                            .setReaderPosition(reader.position()).setArgumentNumber(syntax.getArguments().size() - 1)
                            .setException(exception)
                            .setSuccess(false);
                } else if (reader.position() == primaryContext.getReaderPosition()) {
                    primaryContext.setSyntax(null).setArgumentMap(null).setArgumentNumber(-1).setException(null)
                            .setSuccess(false);
                }
                continue;
            }
            primaryContext.setSyntax(syntax).setArgumentMap(temporaryArgumentMap).setReaderPosition(reader.position())
                    .setArgumentNumber(syntax.getArguments().size() - 1).setException(null).setSuccess(true);

            return primaryContext;

        }

        return primaryContext;
    }

    // Requires at least one whitespace character that occurs at the start of the reader
    private @NotNull Command roamSubcommands(@NotNull Command command, @NotNull StringReader reader, @NotNull List<Command> currentParents) {
        if (command.getSubcommands().isEmpty() || !reader.canRead() || !StringReader.isValidWhitespace(reader.peek())) {
            return command;
        }
        int start = reader.position();
        reader.skipWhitespace();

        Command newCommand = findValidSubcommand(reader, command);

        if (newCommand == null) {
            reader.position(start);
            return command;
        }

        currentParents.add(newCommand);
        return roamSubcommands(command, reader, currentParents);
    }

    private @Nullable Command findValidSubcommand(@NotNull StringReader reader, @NotNull Command currentCommand) {
        List<Command> sub = currentCommand.getSubcommands();

        // Find the longest command
        int longestLength = -1;
        Command longestCommand = null;
        for (int i = sub.size() - 1; i >= 0; i--) {
            Command current = sub.get(i);

            List<String> names = current.getFormattedNames();
            for (int j = names.size() - 1; j >= 0; j--) {
                String name = names.get(i);

                if (name.length() > longestLength &&
                        reader.canRead(name, true) &&
                        (!reader.canRead(name.length() + 1) ||
                                StringReader.isValidWhitespace(reader.peek(name.length()))
                        )) {
                    longestLength = name.length();
                    longestCommand = current;
                }
            }
        }

        if (longestLength == -1) {
            return null;
        }

        reader.skip(longestLength);

        return longestCommand;
    }
}
