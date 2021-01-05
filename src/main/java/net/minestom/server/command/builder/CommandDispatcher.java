package net.minestom.server.command.builder;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.condition.CommandCondition;
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
            for (String alias : command.getAliases()) {
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
     * Parses the given command.
     *
     * @param commandString the command (containing the command name and the args if any)
     * @return the result of the parsing, null if the command doesn't exist
     */
    public CommandResult parse(@NotNull String commandString) {
        commandString = commandString.trim();

        // Split space
        final String spaceRegex = " ";
        final String[] parts = commandString.split(spaceRegex);
        final String commandName = parts[0];

        final String[] args = commandString.replaceFirst(Pattern.quote(commandName), "").trim().split(spaceRegex);

        final Command command = findCommand(commandName);
        // Check if the command exists
        if (command == null)
            return null;

        // Find the used syntax, or check which argument is wrong
        return findCommandResult(command, args);
    }

    /**
     * Check if the command exists, and execute it.
     *
     * @param source        the command source
     * @param commandString the command with the argument(s)
     * @return true if the command executed successfully, false if the command doesn't exist
     */
    public boolean execute(@NotNull CommandSender source, @NotNull String commandString) {
        CommandResult result = parse(commandString);
        if (result != null) {
            result.execute(source, commandString);
        }
        return result != null;
    }

    @NotNull
    public Set<Command> getCommands() {
        return Collections.unmodifiableSet(commands);
    }

    /**
     * GetS the command class associated with the name;
     *
     * @param commandName the command name
     * @return the {@link Command} associated with the name, null if not any
     */
    @Nullable
    public Command findCommand(@NotNull String commandName) {
        commandName = commandName.toLowerCase();
        return commandMap.getOrDefault(commandName, null);
    }

    @NotNull
    private CommandResult findCommandResult(@NotNull Command command, @NotNull String[] args) {
        CommandResult result = new CommandResult();
        result.command = command;

        Arguments executorArgs = new Arguments();

        // The default executor should be used if no argument is provided
        if (args[0].length() == 0) {
            result.executor = command.getDefaultExecutor();
            result.arguments = executorArgs;
            return result;
        }

        // SYNTAXES PARSING

        // All the registered syntaxes of the command
        final Collection<CommandSyntax> syntaxes = command.getSyntaxes();
        // Contains all the fully validated syntaxes (we later find the one with the most amount of arguments)
        List<CommandSyntax> validSyntaxes = new ArrayList<>();
        // Contains the raw string value of each argument that has been validated
        // CommandSyntax - (Argument index/Raw string value)
        Map<CommandSyntax, String[]> syntaxesValues = new HashMap<>();

        // Contains all the syntaxes that are not fully correct, used to later, retrieve the "most correct syntax"
        // Number of correct argument - The data about the failing argument
        TreeMap<Integer, CommandSuggestionHolder> syntaxesSuggestions = new TreeMap<>(Collections.reverseOrder());

        for (CommandSyntax syntax : syntaxes) {
            final Argument<?>[] arguments = syntax.getArguments();
            final String[] argsValues = new String[Byte.MAX_VALUE];

            boolean syntaxCorrect = true;
            // The current index in the raw command string arguments
            int argIndex = 0;

            boolean useRemaining = false;
            // Check the validity of the arguments...
            for (int argCount = 0; argCount < arguments.length; argCount++) {
                final Argument<?> argument = arguments[argCount];
                useRemaining = argument.useRemaining();

                // the correction result of the argument
                int correctionResult = Argument.SUCCESS;
                // true if the arg is valid, false otherwise
                boolean correct = false;
                // the raw string representing the correct argument syntax
                StringBuilder argValue = new StringBuilder();

                if (useRemaining) {
                    final boolean hasArgs = args.length > argIndex;
                    // Verify if there is any string part available
                    if (hasArgs) {
                        // Argument is supposed to take the rest of the command input
                        for (int i = argIndex; i < args.length; i++) {
                            final String arg = args[i];
                            if (argValue.length() > 0)
                                argValue.append(" ");
                            argValue.append(arg);
                        }

                        final String argValueString = argValue.toString();

                        correctionResult = argument.getCorrectionResult(argValueString);
                        if (correctionResult == Argument.SUCCESS) {
                            correct = true;
                            argsValues[argCount] = argValueString;
                        }
                    }
                } else {
                    // Argument is either single-word or can accept optional delimited space(s)
                    for (int i = argIndex; i < args.length; i++) {
                        final String rawArg = args[i];

                        argValue.append(rawArg);

                        final String argValueString = argValue.toString();

                        correctionResult = argument.getCorrectionResult(argValueString);
                        if (correctionResult == Argument.SUCCESS) {
                            correct = true;
                            argsValues[argCount] = argValueString;
                            argIndex = i + 1;
                            break;
                        } else {
                            if (!argument.allowSpace())
                                break;
                            argValue.append(" ");
                        }
                    }
                }

                if (!correct) {
                    // Argument is not correct, add it to the syntax suggestion with the number
                    // of correct argument(s) and do not check the next syntax argument
                    syntaxCorrect = false;
                    CommandSuggestionHolder suggestionHolder = new CommandSuggestionHolder();
                    suggestionHolder.syntax = syntax;
                    suggestionHolder.argValue = argValue.toString();
                    suggestionHolder.correctionResult = correctionResult;
                    suggestionHolder.argIndex = argCount;
                    syntaxesSuggestions.put(argCount, suggestionHolder);
                    break;
                }
            }

            // Add the syntax to the list of valid syntaxes if correct
            if (syntaxCorrect) {
                if (args.length == argIndex || useRemaining) {
                    validSyntaxes.add(syntax);
                    syntaxesValues.put(syntax, argsValues);
                }
            }
        }

        // Check if there is at least one correct syntax
        if (!validSyntaxes.isEmpty()) {
            // Search the syntax with all perfect args
            final CommandSyntax finalSyntax = findMostCorrectSyntax(validSyntaxes, syntaxesValues, executorArgs);
            if (finalSyntax != null) {
                // A fully correct syntax has been found, use it
                result.syntax = finalSyntax;
                result.executor = finalSyntax.getExecutor();
                result.arguments = executorArgs;
                return result;
            }

            // Otherwise, search for the first syntax with an incorrect argument
            for (CommandSyntax syntax : validSyntaxes) {
                final Argument[] arguments = syntax.getArguments();
                final String[] argsValues = syntaxesValues.get(syntax);
                for (int i = 0; i < arguments.length; i++) {
                    final Argument argument = arguments[i];
                    final String argValue = argsValues[i];
                    // Finally parse it
                    final Object parsedValue = argument.parse(argValue);
                    final int conditionResult = argument.getConditionResult(parsedValue);
                    if (conditionResult != Argument.SUCCESS) {
                        // Condition of an argument not correct, use the argument callback if any
                        if (argument.hasErrorCallback()) {
                            result.callback = argument.getCallback();
                            result.value = argValue;
                            result.error = conditionResult;

                            return result;
                        }
                    }
                }
            }

        }

        // No all-correct syntax, find the closest one to use the argument callback
        {
            // Get closest valid syntax
            if (!syntaxesSuggestions.isEmpty()) {
                final int max = syntaxesSuggestions.firstKey(); // number of correct arguments
                // Check if at least 1 argument of the syntax is correct
                if (max > 0) {
                    // Get the data of the closest syntax
                    final CommandSuggestionHolder suggestionHolder = syntaxesSuggestions.get(max);
                    final CommandSyntax syntax = suggestionHolder.syntax;
                    final String argValue = suggestionHolder.argValue;
                    final int correctionResult = suggestionHolder.correctionResult;
                    final int argIndex = suggestionHolder.argIndex;

                    // Found the closest syntax with at least 1 correct argument
                    final Argument<?> argument = syntax.getArguments()[argIndex];
                    if (argument.hasErrorCallback()) {
                        result.callback = argument.getCallback();
                        result.value = argValue;
                        result.error = correctionResult;

                        return result;
                    }
                }
            }
        }

        // Use the default executor at last resort
        result.executor = command.getDefaultExecutor();
        result.arguments = executorArgs;

        return result;
    }

    /**
     * Retrieves from the valid syntax map the arguments condition result and get the one with the most
     * valid arguments.
     *
     * @param validSyntaxes  the list containing all the valid syntaxes
     * @param syntaxesValues the map containing the argument raw string values
     * @param executorArgs   the recipient of the argument parsed values
     * @return the command syntax with all of its arguments correct and with the most arguments count, null if not any
     */
    @Nullable
    private CommandSyntax findMostCorrectSyntax(@NotNull List<CommandSyntax> validSyntaxes,
                                                @NotNull Map<CommandSyntax, String[]> syntaxesValues,
                                                @NotNull Arguments executorArgs) {
        Map<CommandSyntax, Arguments> argumentsValueMap = new HashMap<>();

        CommandSyntax finalSyntax = null;
        int maxArguments = 0;
        for (CommandSyntax syntax : validSyntaxes) {
            Arguments syntaxValues = new Arguments();
            boolean fullyCorrect = true;

            final Argument<?>[] arguments = syntax.getArguments();
            final String[] argsValues = syntaxesValues.get(syntax);
            for (int i = 0; i < arguments.length; i++) {
                final Argument argument = arguments[i];
                final String argValue = argsValues[i];
                // Finally parse it
                final Object parsedValue = argument.parse(argValue);
                final int conditionResult = argument.getConditionResult(parsedValue);
                if (conditionResult == Argument.SUCCESS) {
                    syntaxValues.setArg(argument.getId(), parsedValue);
                } else {
                    // One argument is incorrect, stop the whole syntax check
                    fullyCorrect = false;
                    break;
                }
            }

            final int argumentLength = arguments.length;
            if (fullyCorrect && argumentLength > maxArguments) {
                finalSyntax = syntax;
                maxArguments = argumentLength;
                argumentsValueMap.put(syntax, syntaxValues);
            }
        }

        // Get the arguments values
        if (finalSyntax != null) {
            executorArgs.copy(argumentsValueMap.get(finalSyntax));
        }

        return finalSyntax;
    }

    private static class CommandSuggestionHolder {
        private CommandSyntax syntax;
        private String argValue;
        private int correctionResult;
        private int argIndex;
    }

    /**
     * Represents a {@link Command} ready to be executed (already parsed).
     */
    private static class CommandResult {

        // Command
        private Command command;

        // Command Executor
        private CommandSyntax syntax;

        private CommandExecutor executor;
        private Arguments arguments;

        // Argument Callback
        private ArgumentCallback callback;
        private String value;
        private int error;

        /**
         * Executes the command for the given source.
         * <p>
         * The command will not be executed if {@link Command#getCondition()}
         * is not validated.
         *
         * @param source        the command source
         * @param commandString the command string
         */
        public void execute(@NotNull CommandSender source, @NotNull String commandString) {
            // Global listener
            command.globalListener(source, arguments, commandString);
            // Command condition check
            final CommandCondition condition = command.getCondition();
            if (condition != null) {
                final boolean result = condition.canUse(source, commandString);
                if (!result)
                    return;
            }
            // Condition is respected
            if (executor != null) {
                // An executor has been found

                if (syntax != null) {
                    // The executor is from a syntax
                    final CommandCondition commandCondition = syntax.getCommandCondition();
                    if (commandCondition == null || commandCondition.canUse(source, commandString)) {
                        arguments.retrieveDefaultValues(syntax.getDefaultValuesMap());
                        executor.apply(source, arguments);
                    }
                } else {
                    // The executor is probably the default one
                    executor.apply(source, arguments);
                }
            } else if (callback != null) {
                // No syntax has been validated but the faulty argument with a callback has been found
                // Execute the faulty argument callback
                callback.apply(source, value, error);
            }
        }

    }
}
