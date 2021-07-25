package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument that maps an existing argument to a value.
 *
 * @param <T> The type of this argument
 */
public class ArgumentFilter<T> extends Argument<T> {

    final Argument<T> argument;
    final Filterer<T> filterer;

    protected ArgumentFilter(@NotNull Argument<T> argument, @NotNull Filterer<T> filterer) {
        super(argument.getId(), argument.allowSpace(), argument.useRemaining());

        if (argument.getSuggestionCallback() != null)
            this.setSuggestionCallback(argument.getSuggestionCallback());

        if (argument.getDefaultValue() != null)
            this.setDefaultValue(argument.getDefaultValue());

        this.argument = argument;
        this.filterer = filterer;
    }

    @Override
    public @NotNull T parse(@NotNull String input) throws ArgumentSyntaxException {
        T result = argument.parse(input);

        filterer.check(result);

        return result;
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        argument.processNodes(nodeMaker, executable);
    }

    /**
     * Represents a lambda that can filter an input and return the input
     * hat also allows the throwing of ArgumentSyntaxException
     *
     * @param <I> The input expected from the Argument
     */
    @FunctionalInterface
    public interface Filterer<I> {

        /**
         * Accepts I data from the argument and throws an error if the input is invalid.
         *
         * @param i The input processed from an argument
         * @throws ArgumentSyntaxException If the input does not match the conditions required for filtering
         *                                 (E.X. an int that must be prime)
         */
        void check(I i) throws ArgumentSyntaxException;

    }

}
