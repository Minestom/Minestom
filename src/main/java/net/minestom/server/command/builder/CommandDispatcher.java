package net.minestom.server.command.builder;

import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Class responsible for parsing {@link Command}.
 */
public class CommandDispatcher {

    private final Map<String, Command> commandMap = new HashMap<>();
    private final Set<Command> commands = new HashSet<>();

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
    }

    /**
     * Checks if the command exists, and execute it.
     *
     * @param source        the command source
     * @param commandString the command with the argument(s)
     * @return the command result
     */
    @NotNull
    public CommandResult execute(@NotNull CommandSender source, @NotNull String commandString) {
        CommandResult commandResult = parse(commandString);
        ParsedCommand parsedCommand = commandResult.parsedCommand;
        if (parsedCommand != null) {
            commandResult.commandData = parsedCommand.execute(source, commandString);
        }
        return commandResult;
    }

    /**
     * Parses the given command.
     *
     * @param commandString the command (containing the command name and the args if any)
     * @return the parsing result
     */
    @NotNull
    public CommandResult parse(@NotNull String commandString) {
        commandString = commandString.trim();

        // Split space
        final String[] parts = commandString.split(StringUtils.SPACE);
        final String commandName = parts[0];

        final Command command = findCommand(commandName);
        // Check if the command exists
        if (command == null) {
            return CommandResult.of(CommandResult.Type.UNKNOWN, commandName);
        }

        // Removes the command's name + the space after
        final String[] args = commandString.replaceFirst(Pattern.quote(commandName), "").trim().split(StringUtils.SPACE);

        // Find the used syntax
        CommandResult result = new CommandResult();
        result.input = commandString;
        ParsedCommand parsedCommand = findParsedCommand(command, args);
        if (parsedCommand != null && parsedCommand.executor != null) {
            // Syntax found
            result.type = CommandResult.Type.SUCCESS;
            result.parsedCommand = parsedCommand;
        } else {
            // Syntax not found, use the default executor (if any)
            result.type = CommandResult.Type.INVALID_SYNTAX;
            result.parsedCommand = ParsedCommand.withDefaultExecutor(command);
        }
        return result;
    }

    @NotNull
    public Set<Command> getCommands() {
        return Collections.unmodifiableSet(commands);
    }

    /**
     * Gets the command class associated with the name.
     *
     * @param commandName the command name
     * @return the {@link Command} associated with the name, null if not any
     */
    @Nullable
    public Command findCommand(@NotNull String commandName) {
        commandName = commandName.toLowerCase();
        return commandMap.getOrDefault(commandName, null);
    }

    @Nullable
    private ParsedCommand findParsedCommand(@NotNull Command command, @NotNull String[] args) {
        ParsedCommand parsedCommand = new ParsedCommand();
        parsedCommand.command = command;

        // The default executor should be used if no argument is provided
        {
            final CommandExecutor defaultExecutor = command.getDefaultExecutor();
            if (defaultExecutor != null && args[0].length() == 0) {
                parsedCommand.executor = defaultExecutor;
                parsedCommand.arguments = new Arguments();
                return parsedCommand;
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
            // List of all arguments in the current syntax
            final Argument<?>[] arguments = syntax.getArguments();
            // Empty object containing the maximum amount of argument results of the syntax.
            final List<Object> argsValues = new ArrayList<>(arguments.length);

            boolean syntaxCorrect = true;
            // The current index in the raw command string arguments
            int splitIndex = 0;

            boolean useRemaining = false;
            // Check the validity of the arguments...
            for (int argCount = 0; argCount < arguments.length; argCount++) {
                final boolean lastArgumentIteration = argCount + 1 == arguments.length;
                final Argument<?> argument = arguments[argCount];
                useRemaining = argument.useRemaining();

                // the parsed argument value, null if incorrect
                Object parsedValue;
                // the argument exception, null if the input is correct
                ArgumentSyntaxException argumentSyntaxException = null;
                // true if the arg is valid, false otherwise
                boolean correct = false;
                // the raw string representing the correct argument syntax
                StringBuilder argValue = new StringBuilder();

                if (useRemaining) {
                    final boolean hasArgs = args.length > splitIndex;
                    // Verify if there is any string part available
                    if (hasArgs) {
                        // Argument is supposed to take the rest of the command input
                        for (int i = splitIndex; i < args.length; i++) {
                            final String arg = args[i];
                            if (argValue.length() > 0)
                                argValue.append(StringUtils.SPACE);
                            argValue.append(arg);
                        }

                        final String argValueString = argValue.toString();

                        try {
                            parsedValue = argument.parse(argValueString);
                            correct = true;
                            argsValues.add(parsedValue);
                        } catch (ArgumentSyntaxException exception) {
                            argumentSyntaxException = exception;
                        }
                    }
                } else {
                    // Argument is either single-word or can accept optional delimited space(s)
                    for (int i = splitIndex; i < args.length; i++) {
                        final String rawArg = args[i];

                        argValue.append(rawArg);

                        final String argValueString = argValue.toString();

                        try {
                            parsedValue = argument.parse(argValueString);

                            // Prevent quitting the parsing too soon if the argument
                            // does not allow space
                            if (lastArgumentIteration && i + 1 < args.length) {
                                if (!argument.allowSpace())
                                    break;
                                argValue.append(StringUtils.SPACE);
                                continue;
                            }

                            correct = true;
                            argsValues.add(parsedValue);
                            splitIndex = i + 1;
                            break;
                        } catch (ArgumentSyntaxException exception) {
                            argumentSyntaxException = exception;

                            if (!argument.allowSpace())
                                break;
                            argValue.append(StringUtils.SPACE);
                        }
                    }
                }

                if (!correct) {
                    // Argument is not correct, add it to the syntax suggestion with the number
                    // of correct argument(s) and do not check the next syntax argument
                    syntaxCorrect = false;
                    CommandSuggestionHolder suggestionHolder = new CommandSuggestionHolder();
                    suggestionHolder.syntax = syntax;
                    suggestionHolder.argumentSyntaxException = argumentSyntaxException;
                    suggestionHolder.argIndex = argCount;
                    syntaxesSuggestions.put(argCount, suggestionHolder);
                    break;
                }
            }

            // Add the syntax to the list of valid syntaxes if correct
            if (syntaxCorrect) {
                if (arguments.length == argsValues.size() || useRemaining) {
                    ValidSyntaxHolder validSyntaxHolder = new ValidSyntaxHolder();
                    validSyntaxHolder.syntax = syntax;
                    validSyntaxHolder.argumentsValue = argsValues;

                    validSyntaxes.add(validSyntaxHolder);
                }
            }
        }

        // Check if there is at least one correct syntax
        if (!validSyntaxes.isEmpty()) {
            Arguments executorArgs = new Arguments();
            // Search the syntax with all perfect args
            final CommandSyntax finalSyntax = findMostCorrectSyntax(validSyntaxes, executorArgs);
            if (finalSyntax != null) {
                // A fully correct syntax has been found, use it
                parsedCommand.syntax = finalSyntax;
                parsedCommand.executor = finalSyntax.getExecutor();
                parsedCommand.arguments = executorArgs;
                return parsedCommand;
            }

        }

        // No all-correct syntax, find the closest one to use the argument callback
        {
            // Get closest valid syntax
            if (!syntaxesSuggestions.isEmpty()) {
                final int max = syntaxesSuggestions.firstIntKey(); // number of correct arguments in the most correct syntax
                final CommandSuggestionHolder suggestionHolder = syntaxesSuggestions.get(max);
                final CommandSyntax syntax = suggestionHolder.syntax;
                final ArgumentSyntaxException argumentSyntaxException = suggestionHolder.argumentSyntaxException;
                final int argIndex = suggestionHolder.argIndex;

                // Found the closest syntax with at least 1 correct argument
                final Argument<?> argument = syntax.getArguments()[argIndex];
                if (argument.hasErrorCallback()) {
                    parsedCommand.callback = argument.getCallback();
                    parsedCommand.argumentSyntaxException = argumentSyntaxException;

                    return parsedCommand;
                }
            }
        }

        // No syntax found
        return null;
    }

    /**
     * Retrieves from the valid syntax map the arguments condition result and get the one with the most
     * valid arguments.
     *
     * @param validSyntaxes the list containing all the valid syntaxes
     * @param executorArgs  the recipient of the argument parsed values
     * @return the command syntax with all of its arguments correct and with the most arguments count, null if not any
     */
    @Nullable
    private CommandSyntax findMostCorrectSyntax(@NotNull List<ValidSyntaxHolder> validSyntaxes,
                                                @NotNull Arguments executorArgs) {
        CommandSyntax finalSyntax = null;
        int maxArguments = 0;
        Arguments finalArguments = null;

        for (ValidSyntaxHolder validSyntaxHolder : validSyntaxes) {
            final CommandSyntax syntax = validSyntaxHolder.syntax;

            final Argument<?>[] arguments = syntax.getArguments();
            final int argumentsCount = arguments.length;
            final List<Object> argsValues = validSyntaxHolder.argumentsValue;

            final int argsSize = argsValues.size();

            if (argsSize > maxArguments) {
                finalSyntax = syntax;
                maxArguments = argsSize;

                // Fill arguments map
                Arguments syntaxValues = new Arguments();
                for (int i = 0; i < argumentsCount; i++) {
                    final Argument<?> argument = arguments[i];
                    final Object argumentValue = argsValues.get(i);

                    syntaxValues.setArg(argument.getId(), argumentValue);
                }
                finalArguments = syntaxValues;
            }
        }

        // Get the arguments values
        if (finalSyntax != null) {
            executorArgs.copy(finalArguments);
        }

        return finalSyntax;
    }

    /**
     * Holds the data of a validated syntax.
     */
    private static class ValidSyntaxHolder {
        private CommandSyntax syntax;
        /**
         * (Argument index/Argument parsed object)
         */
        private List<Object> argumentsValue;
    }

    /**
     * Holds the data of an invalidated syntax.
     */
    private static class CommandSuggestionHolder {
        private CommandSyntax syntax;
        private ArgumentSyntaxException argumentSyntaxException;
        private int argIndex;
    }
}
