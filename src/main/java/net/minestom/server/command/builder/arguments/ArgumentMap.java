package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument that maps an existing argument to a value.
 *
 * @param <I> The input (any object)
 * @param <O> The output (any object)
 */
public class ArgumentMap<I, O> extends Argument<O> {

    final Argument<I> argument;
    final Mapper<I, O> mapper;

    protected ArgumentMap(@NotNull Argument<I> argument, @NotNull Mapper<I, O> mapper) {
        super(argument.getId(), argument.allowSpace(), argument.useRemaining());

        this.argument = argument;
        this.mapper = mapper;
    }

    @Override
    public @NotNull O parse(@NotNull String input) throws ArgumentSyntaxException {
        return mapper.accept(argument.parse(input));
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        argument.processNodes(nodeMaker, executable);
    }

    /**
     * Represents a lambda that can turn an input into an output
     * that also allows the throwing of ArgumentSyntaxException
     *
     * @param <I> The input expected from the Argument
     * @param <O> The desired output type from this lambda.
     */
    @FunctionalInterface
    public interface Mapper<I, O> {

        /**
         * Accepts I data from the argument and returns O output
         *
         * @param i The input processed from an argument
         * @return The complex data type that came as a result from this argument
         * @throws ArgumentSyntaxException If the input can not be turned into the desired output
         *                                 (E.X. an invalid extension name)
         */
        O accept(I i) throws ArgumentSyntaxException;

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
        void accept(I i) throws ArgumentSyntaxException;

    }

}
