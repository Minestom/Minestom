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
        this.commandMap.put(command.getName(), command);
        for (String alias : command.getAliases()) {
            this.commandMap.put(alias, command);
        }
        this.commands.add(command);
    }

    public CommandResult parse(String commandString) {
        commandString = commandString.trim();

        // Split space
        String spaceRegex = " ";
        String[] splitted = commandString.split(spaceRegex);
        String commandName = splitted[0];

        String[] args = commandString.replaceFirst(Pattern.quote(commandName), "").trim().split(spaceRegex);

        Command command = findCommand(commandName);
        // TODO change return
        if (command == null)
            return null;

        return findCommandResult(command, args);
    }

    public void execute(CommandSender source, String commandString) {
        CommandResult result = parse(commandString);
        result.execute(source);
    }

    public Set<Command> getCommands() {
        return Collections.unmodifiableSet(commands);
    }

    private Command findCommand(String commandName) {
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
        Collection<CommandSyntax> syntaxes = command.getSyntaxes();
        List<CommandSyntax> validSyntaxes = new ArrayList<>();
        Map<CommandSyntax, String[]> syntaxesValues = new HashMap<>();

        TreeMap<Integer, CommandSuggestionHolder> syntaxesSuggestions = new TreeMap<>(Collections.reverseOrder());

        for (CommandSyntax syntax : syntaxes) {
            Argument[] arguments = syntax.getArguments();
            String[] argsValues = new String[arguments.length];

            boolean syntaxCorrect = true;
            int argIndex = 0;

            boolean useRemaining = false;
            for (int argCount = 0; argCount < syntax.getArguments().length; argCount++) {
                Argument argument = syntax.getArguments()[argCount];
                useRemaining = argument.useRemaining();

                int correctionResult = Argument.SUCCESS;
                boolean correct = false;
                String argValue = "";

                if (useRemaining) {
                    for (int i = argIndex; i < args.length; i++) {
                        String arg = args[i];
                        if (argValue.length() > 0)
                            argValue += " ";
                        argValue += arg;
                    }

                    correctionResult = argument.getCorrectionResult(argValue);
                    if (correctionResult == Argument.SUCCESS) {
                        correct = true;
                        argsValues[argIndex] = argValue;
                    }
                } else {
                    for (int i = argIndex; i < args.length; i++) {
                        String arg = args[i];

                        argValue += arg;

                        correctionResult = argument.getCorrectionResult(argValue);
                        if (correctionResult == Argument.SUCCESS) {
                            correct = true;
                            argsValues[argIndex] = argValue;
                            argIndex = i + 1;
                            break;
                        } else {
                            if (!argument.allowSpace())
                                break;
                            argValue += " ";
                        }
                    }
                }

                if (correct) {
                    continue;
                } else {
                    syntaxCorrect = false;
                    CommandSuggestionHolder suggestionHolder = new CommandSuggestionHolder();
                    suggestionHolder.syntax = syntax;
                    suggestionHolder.argValue = argValue;
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
            Argument[] arguments = finalSyntax.getArguments();
            String[] argsValues = syntaxesValues.get(finalSyntax);
            for (int i = 0; i < arguments.length; i++) {
                Argument argument = arguments[i];
                String argValue = argsValues[i];
                // Finally parse it
                Object parsedValue = argument.parse(argValue);
                int conditionResult = argument.getConditionResult(parsedValue);
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
                int max = syntaxesSuggestions.firstKey();
                CommandSuggestionHolder suggestionHolder = syntaxesSuggestions.get(max);
                CommandSyntax syntax = suggestionHolder.syntax;
                String argValue = suggestionHolder.argValue;
                int correctionResult = suggestionHolder.correctionResult;
                int argIndex = suggestionHolder.argIndex;

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

    private class CommandSuggestionHolder {
        private CommandSyntax syntax;
        private String argValue;
        private int correctionResult;
        private int argIndex;

    }

    private class CommandResult {

        // Command
        private Command command;

        // Command Executor
        private CommandExecutor executor;
        private Arguments arguments;

        // Argument Callback
        private ArgumentCallback callback;
        private String value;
        private int error;

        public void execute(CommandSender source) {
            // Condition check
            CommandCondition condition = command.getCondition();
            if (condition != null) {
                boolean result = condition.apply(source);
                if (!result)
                    return;
            }
            // Condition is respected
            if (executor != null) {
                executor.apply(source, arguments);
            } else if (callback != null) {
                callback.apply(source, value, error);
            }
        }

    }
}
