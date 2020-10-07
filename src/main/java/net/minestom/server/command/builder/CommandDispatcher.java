package net.minestom.server.command.builder;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.condition.CommandCondition;

import java.util.*;
import java.util.regex.Pattern;

public class CommandDispatcher {

    private Map<String, Command> commandMap = new HashMap<>();
    private Set<Command> commands = new HashSet<>();

    public void register(Command command) {
        this.commandMap.put(command.getName().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            this.commandMap.put(alias.toLowerCase(), command);
        }
        this.commands.add(command);
    }

    /**
     * Parse the given command
     *
     * @param commandString the command (containing the command name and the args if any)
     * @return the result of the parsing, null if the command doesn't exist
     */
    public CommandResult parse(String commandString) {
        commandString = commandString.trim();

        // Split space
        final String spaceRegex = " ";
        final String[] splitted = commandString.split(spaceRegex);
        final String commandName = splitted[0];

        final String[] args = commandString.replaceFirst(Pattern.quote(commandName), "").trim().split(spaceRegex);

        final Command command = findCommand(commandName);
        // Check if the command exists
        if (command == null)
            return null;

        // Find the used syntax, or check which argument is wrong
        return findCommandResult(command, args);
    }

    public void execute(CommandSender source, String commandString) {
        CommandResult result = parse(commandString);
        result.execute(source, commandString);
    }

    public Set<Command> getCommands() {
        return Collections.unmodifiableSet(commands);
    }

    /**
     * Get the command class associated with its name
     *
     * @param commandName the command name
     * @return the {@link Command} associated with the name, null if not any
     */
    public Command findCommand(String commandName) {
        commandName = commandName.toLowerCase();
        return commandMap.containsKey(commandName) ? commandMap.get(commandName) : null;
    }

    private CommandResult findCommandResult(Command command, String[] args) {
        CommandResult result = new CommandResult();
        result.command = command;

        Arguments executorArgs = new Arguments();

        // Default executor
        // Check if args array is empty
        if (args[0].length() == 0) {
            result.executor = command.getDefaultExecutor();
            result.arguments = executorArgs;
            return result;
        }


        // Find syntax
        final Collection<CommandSyntax> syntaxes = command.getSyntaxes();
        List<CommandSyntax> validSyntaxes = new ArrayList<>();
        Map<CommandSyntax, String[]> syntaxesValues = new HashMap<>();

        TreeMap<Integer, CommandSuggestionHolder> syntaxesSuggestions = new TreeMap<>(Collections.reverseOrder());

        for (CommandSyntax syntax : syntaxes) {
            final Argument[] arguments = syntax.getArguments();
            final String[] argsValues = new String[arguments.length];

            boolean syntaxCorrect = true;
            int argIndex = 0;

            boolean useRemaining = false;
            for (int argCount = 0; argCount < syntax.getArguments().length; argCount++) {
                final Argument argument = syntax.getArguments()[argCount];
                useRemaining = argument.useRemaining();

                // the correction result of the argument
                int correctionResult = Argument.SUCCESS;
                // true if the arg is valid, false otherwise
                boolean correct = false;
                // the raw string representing the correct argument syntax
                StringBuilder argValue = new StringBuilder();

                if (useRemaining) {
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
                        argsValues[argIndex] = argValueString;
                    }
                } else {
                    for (int i = argIndex; i < args.length; i++) {
                        final String arg = args[i];

                        argValue.append(arg);

                        final String argValueString = argValue.toString();

                        correctionResult = argument.getCorrectionResult(argValueString);
                        if (correctionResult == Argument.SUCCESS) {
                            correct = true;
                            argsValues[argIndex] = argValueString;
                            argIndex = i + 1;
                            break;
                        } else {
                            if (!argument.allowSpace())
                                break;
                            argValue.append(" ");
                        }
                    }
                }

                if (correct) {
                    continue;
                } else {
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
            if (syntaxCorrect) {
                if (args.length == argIndex || useRemaining) {
                    validSyntaxes.add(syntax);
                    syntaxesValues.put(syntax, argsValues);
                }
            }
        }

        // Find the valid syntax with the most of args
        CommandSyntax finalSyntax = null;
        for (CommandSyntax syntax : validSyntaxes) {
            if (finalSyntax == null || finalSyntax.getArguments().length < syntax.getArguments().length) {
                finalSyntax = syntax;
            }
        }

        // Verify args conditions of finalSyntax
        if (finalSyntax != null) {
            final Argument[] arguments = finalSyntax.getArguments();
            final String[] argsValues = syntaxesValues.get(finalSyntax);
            for (int i = 0; i < arguments.length; i++) {
                final Argument argument = arguments[i];
                final String argValue = argsValues[i];
                // Finally parse it
                final Object parsedValue = argument.parse(argValue);
                final int conditionResult = argument.getConditionResult(parsedValue);
                if (conditionResult == Argument.SUCCESS) {
                    executorArgs.setArg(argument.getId(), parsedValue);
                } else {
                    result.callback = argument.getCallback();
                    result.value = argValue;
                    result.error = conditionResult;

                    return result;
                }
            }
        }

        // If command isn't correct, find the closest
        if (finalSyntax == null) {
            // Get closest valid syntax
            if (!syntaxesSuggestions.isEmpty()) {
                final int max = syntaxesSuggestions.firstKey();
                final CommandSuggestionHolder suggestionHolder = syntaxesSuggestions.get(max);
                final CommandSyntax syntax = suggestionHolder.syntax;
                final String argValue = suggestionHolder.argValue;
                final int correctionResult = suggestionHolder.correctionResult;
                final int argIndex = suggestionHolder.argIndex;

                if (argValue.length() > 0) {
                    Argument argument = syntax.getArguments()[argIndex];
                    result.callback = argument.getCallback();
                    result.value = argValue;
                    result.error = correctionResult;
                } else {
                    result.executor = command.getDefaultExecutor();
                    result.arguments = executorArgs;
                }

                return result;
            }
        }

        // Use finalSyntax, or default executor if no syntax has been found
        result.executor = finalSyntax == null ? command.getDefaultExecutor() : finalSyntax.getExecutor();
        result.arguments = executorArgs;

        return result;
    }

    private static class CommandSuggestionHolder {
        private CommandSyntax syntax;
        private String argValue;
        private int correctionResult;
        private int argIndex;

    }

    /**
     * Represents a command ready to be executed (already parsed)
     */
    private static class CommandResult {

        // Command
        private Command command;

        // Command Executor
        private CommandExecutor executor;
        private Arguments arguments;

        // Argument Callback
        private ArgumentCallback callback;
        private String value;
        private int error;

        /**
         * Execute the command for the given source
         * <p>
         * The command will not be executed if the {@link CommandCondition} of the command
         * is not validated
         *
         * @param source        the command source
         * @param commandString the command string
         */
        public void execute(CommandSender source, String commandString) {
            // Global listener
            command.globalListener(source, arguments, commandString);
            // Condition check
            final CommandCondition condition = command.getCondition();
            if (condition != null) {
                final boolean result = condition.apply(source);
                if (!result)
                    return;
            }
            // Condition is respected
            if (executor != null) {
                // An executor has been found
                executor.apply(source, arguments);
            } else if (callback != null) {
                // No syntax has been validated but the faulty argument with a callback has been found
                // Execute the faulty argument callback
                callback.apply(source, value, error);
            }
        }

    }
}
