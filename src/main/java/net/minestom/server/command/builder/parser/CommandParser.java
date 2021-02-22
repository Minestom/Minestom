package net.minestom.server.command.builder.parser;

import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandParser {

    public static void parse(@Nullable CommandSyntax syntax, @NotNull Argument<?>[] commandArguments, @NotNull String[] inputArguments,
                             @Nullable List<ValidSyntaxHolder> validSyntaxes,
                             @Nullable Int2ObjectRBTreeMap<CommandSuggestionHolder> syntaxesSuggestions) {
        final Map<Argument<?>, Object> argsValues = new HashMap<>();

        boolean syntaxCorrect = true;
        // The current index in the raw command string arguments
        int splitIndex = 0;

        boolean useRemaining = false;
        // Check the validity of the arguments...
        for (int argCount = 0; argCount < commandArguments.length; argCount++) {
            final boolean lastArgumentIteration = argCount + 1 == commandArguments.length;
            final Argument<?> argument = commandArguments[argCount];
            useRemaining = argument.useRemaining();

            final boolean end = splitIndex == inputArguments.length;
            if (end) // True if there is no input to analyze left
                break;

            // the parsed argument value, null if incorrect
            Object parsedValue;
            // the argument exception, null if the input is correct
            ArgumentSyntaxException argumentSyntaxException = null;
            // true if the arg is valid, false otherwise
            boolean correct = false;
            // the raw string representing the correct argument syntax
            StringBuilder argValue = new StringBuilder();

            if (useRemaining) {
                final boolean hasArgs = inputArguments.length > splitIndex;
                // Verify if there is any string part available
                if (hasArgs) {
                    // Argument is supposed to take the rest of the command input
                    for (int i = splitIndex; i < inputArguments.length; i++) {
                        final String arg = inputArguments[i];
                        if (argValue.length() > 0)
                            argValue.append(StringUtils.SPACE);
                        argValue.append(arg);
                    }

                    final String argValueString = argValue.toString();

                    try {
                        parsedValue = argument.parse(argValueString);
                        correct = true;
                        argsValues.put(argument, parsedValue);
                    } catch (ArgumentSyntaxException exception) {
                        argumentSyntaxException = exception;
                    }
                }
            } else {
                // Argument is either single-word or can accept optional delimited space(s)
                for (int i = splitIndex; i < inputArguments.length; i++) {
                    final String rawArg = inputArguments[i];

                    argValue.append(rawArg);

                    final String argValueString = argValue.toString();

                    try {
                        parsedValue = argument.parse(argValueString);

                        // Prevent quitting the parsing too soon if the argument
                        // does not allow space
                        if (lastArgumentIteration && i + 1 < inputArguments.length) {
                            if (!argument.allowSpace())
                                break;
                            argValue.append(StringUtils.SPACE);
                            continue;
                        }

                        correct = true;
                        argsValues.put(argument, parsedValue);
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
                if (syntaxesSuggestions != null) {
                    CommandSuggestionHolder suggestionHolder = new CommandSuggestionHolder();
                    suggestionHolder.syntax = syntax;
                    suggestionHolder.argumentSyntaxException = argumentSyntaxException;
                    suggestionHolder.argIndex = argCount;
                    syntaxesSuggestions.put(argCount, suggestionHolder);
                }
                break;
            }
        }

        // Add the syntax to the list of valid syntaxes if correct
        if (syntaxCorrect) {
            if (commandArguments.length == argsValues.size() || useRemaining) {
                if (validSyntaxes != null) {
                    ValidSyntaxHolder validSyntaxHolder = new ValidSyntaxHolder();
                    validSyntaxHolder.syntax = syntax;
                    validSyntaxHolder.argumentsValue = argsValues;

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
     * @param executorArgs  the recipient of the argument parsed values
     * @return the command syntax with all of its arguments correct and with the most arguments count, null if not any
     */
    @Nullable
    public static ValidSyntaxHolder findMostCorrectSyntax(@NotNull List<ValidSyntaxHolder> validSyntaxes,
                                                          @NotNull Arguments executorArgs) {
        if (validSyntaxes.isEmpty()) {
            return null;
        }

        ValidSyntaxHolder finalSyntax = null;
        int maxArguments = 0;
        Arguments finalArguments = null;

        for (ValidSyntaxHolder validSyntaxHolder : validSyntaxes) {
            final Map<Argument<?>, Object> argsValues = validSyntaxHolder.argumentsValue;

            final int argsSize = argsValues.size();

            // Check if the syntax has more valid arguments
            if (argsSize > maxArguments) {
                finalSyntax = validSyntaxHolder;
                maxArguments = argsSize;

                // Fill arguments map
                Arguments syntaxValues = new Arguments();
                for (Map.Entry<Argument<?>, Object> entry : argsValues.entrySet()) {
                    final Argument<?> argument = entry.getKey();
                    final Object argumentValue = entry.getValue();

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

}
