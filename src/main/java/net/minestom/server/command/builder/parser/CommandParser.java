package net.minestom.server.command.builder.parser;

import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static net.minestom.server.command.builder.parser.ArgumentParser.validate;

/**
 * Class used to parse complete command inputs.
 */
public class CommandParser {

    private static final CommandManager COMMAND_MANAGER = MinecraftServer.getCommandManager();

    @Nullable
    public static CommandQueryResult findCommand(@Nullable Command parentCommand, @NotNull String commandName, @NotNull String[] args) {
        Command command = parentCommand == null ? COMMAND_MANAGER.getDispatcher().findCommand(commandName) : parentCommand;
        if (command == null) {
            return null;
        }

        CommandQueryResult commandQueryResult = new CommandQueryResult();
        commandQueryResult.command = command;
        commandQueryResult.commandName = commandName;
        commandQueryResult.args = args;

        // Search for subcommand
        if (args.length > 0) {
            final String subCommandName = args[0];
            for (Command subcommand : command.getSubcommands()) {
                if (Command.isValidName(subcommand, subCommandName)) {
                    final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                    commandQueryResult.command = subcommand;
                    commandQueryResult.commandName = subCommandName;
                    commandQueryResult.args = subArgs;
                    return findCommand(subcommand, subCommandName, subArgs);
                }
            }
        }

        return commandQueryResult;
    }

    @Nullable
    public static CommandQueryResult findCommand(@NotNull String input) {
        final String[] parts = input.split(StringUtils.SPACE);
        final String commandName = parts[0];

        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        return CommandParser.findCommand(null, commandName, args);
    }

    public static void parse(@Nullable CommandSyntax syntax, @NotNull Argument<?>[] commandArguments, @NotNull String[] inputArguments,
                             @NotNull String commandString,
                             @Nullable List<ValidSyntaxHolder> validSyntaxes,
                             @Nullable Int2ObjectRBTreeMap<CommandSuggestionHolder> syntaxesSuggestions) {
        final Map<Argument<?>, ArgumentParser.ArgumentResult> argumentValueMap = new HashMap<>();

        boolean syntaxCorrect = true;
        // The current index in the raw command string arguments
        int inputIndex = 0;

        boolean useRemaining = false;
        // Check the validity of the arguments...
        for (int argIndex = 0; argIndex < commandArguments.length; argIndex++) {
            final Argument<?> argument = commandArguments[argIndex];
            ArgumentParser.ArgumentResult argumentResult = validate(argument, commandArguments, argIndex, inputArguments, inputIndex);
            if (argumentResult == null) {
                break;
            }

            // Update local var
            useRemaining = argumentResult.useRemaining;
            inputIndex = argumentResult.inputIndex;

            if (argumentResult.correct) {
                argumentValueMap.put(argumentResult.argument, argumentResult);
            } else {
                // Argument is not correct, add it to the syntax suggestion with the number
                // of correct argument(s) and do not check the next syntax argument
                syntaxCorrect = false;
                if (syntaxesSuggestions != null) {
                    CommandSuggestionHolder suggestionHolder = new CommandSuggestionHolder();
                    suggestionHolder.syntax = syntax;
                    suggestionHolder.argumentSyntaxException = argumentResult.argumentSyntaxException;
                    suggestionHolder.argIndex = argIndex;
                    syntaxesSuggestions.put(argIndex, suggestionHolder);
                }
                break;
            }
        }

        // Add the syntax to the list of valid syntaxes if correct
        if (syntaxCorrect) {
            if (commandArguments.length == argumentValueMap.size() || useRemaining) {
                if (validSyntaxes != null) {
                    ValidSyntaxHolder validSyntaxHolder = new ValidSyntaxHolder();
                    validSyntaxHolder.commandString = commandString;
                    validSyntaxHolder.syntax = syntax;
                    validSyntaxHolder.argumentResults = argumentValueMap;

                    validSyntaxes.add(validSyntaxHolder);
                }
            }
        }
    }

    /**
     * Retrieves from the valid syntax map the arguments condition result and get the one with the most
     * valid arguments.
     *
     * @param validSyntaxes the list containing all the valid syntaxes
     * @param context       the recipient of the argument parsed values
     * @return the command syntax with all of its arguments correct and with the most arguments count, null if not any
     */
    @Nullable
    public static ValidSyntaxHolder findMostCorrectSyntax(@NotNull List<ValidSyntaxHolder> validSyntaxes,
                                                          @NotNull CommandContext context) {
        if (validSyntaxes.isEmpty()) {
            return null;
        }

        ValidSyntaxHolder finalSyntax = null;
        int maxArguments = 0;
        CommandContext finalContext = null;

        for (ValidSyntaxHolder validSyntaxHolder : validSyntaxes) {
            final Map<Argument<?>, ArgumentParser.ArgumentResult> argsValues = validSyntaxHolder.argumentResults;

            final int argsSize = argsValues.size();

            // Check if the syntax has more valid arguments
            if (argsSize > maxArguments) {
                finalSyntax = validSyntaxHolder;
                maxArguments = argsSize;

                // Fill arguments map
                finalContext = new CommandContext(validSyntaxHolder.commandString);
                for (var entry : argsValues.entrySet()) {
                    final Argument<?> argument = entry.getKey();
                    final ArgumentParser.ArgumentResult argumentResult = entry.getValue();
                    finalContext.setArg(argument.getId(), argumentResult.parsedValue, argumentResult.rawArg);
                }
            }
        }

        // Get the arguments values
        if (finalSyntax != null) {
            context.copy(finalContext);
        }

        return finalSyntax;
    }

    @Nullable
    public static ArgumentQueryResult findEligibleArgument(@NotNull Command command, String[] args, String commandString,
                                                           boolean trailingSpace, boolean forceCorrect,
                                                           Predicate<CommandSyntax> syntaxPredicate,
                                                           Predicate<Argument<?>> argumentPredicate) {
        final Collection<CommandSyntax> syntaxes = command.getSyntaxes();

        Int2ObjectRBTreeMap<ArgumentQueryResult> suggestions = new Int2ObjectRBTreeMap<>(Collections.reverseOrder());

        for (CommandSyntax syntax : syntaxes) {
            if (!syntaxPredicate.test(syntax)) {
                continue;
            }

            final CommandContext context = new CommandContext(commandString);

            final Argument<?>[] commandArguments = syntax.getArguments();
            int inputIndex = 0;

            ArgumentQueryResult maxArg = null;
            int maxArgIndex = 0;
            for (int argIndex = 0; argIndex < commandArguments.length; argIndex++) {
                Argument<?> argument = commandArguments[argIndex];
                ArgumentParser.ArgumentResult argumentResult = validate(argument, commandArguments, argIndex, args, inputIndex);
                if (argumentResult == null) {
                    // Nothing to analyze, create a dummy object
                    argumentResult = new ArgumentParser.ArgumentResult();
                    argumentResult.argument = argument;
                    argumentResult.correct = false;
                    argumentResult.inputIndex = inputIndex;
                    argumentResult.rawArg = "";
                }

                // Update local var
                inputIndex = argumentResult.inputIndex;

                if (argumentResult.correct) {
                    // Fill context
                    context.setArg(argument.getId(), argumentResult.parsedValue, argumentResult.rawArg);
                }

                // Save result
                if ((!forceCorrect || argumentResult.correct) &&
                        argumentPredicate.test(argument)) {
                    ArgumentQueryResult queryResult = new ArgumentQueryResult();
                    queryResult.syntax = syntax;
                    queryResult.argument = argument;
                    queryResult.context = context;
                    queryResult.input = argumentResult.rawArg;

                    maxArg = queryResult;
                    maxArgIndex = argIndex;
                }

                // Don't compute following arguments if the syntax is incorrect
                if (!argumentResult.correct) {
                    break;
                }

                // Don't compute unrelated arguments
                final boolean isLast = inputIndex == args.length;
                if (isLast && !trailingSpace) {
                    break;
                }

            }
            if (maxArg != null) {
                suggestions.put(maxArgIndex, maxArg);
            }
        }

        if (suggestions.isEmpty()) {
            // No suggestion
            return null;
        }

        final int max = suggestions.firstIntKey();
        return suggestions.get(max);
    }

}
