package net.minestom.server.command.builder;

import net.minestom.server.command.builder.exception.ArgumentSyntaxException;

/**
 * Represents a lambda that can turn an input into an output
 * that also allows the throwing of ArgumentSyntaxException
 *
 * @param <I> The input expected from the Argument
 * @param <O> The desired output type from this lambda.
 */
@FunctionalInterface
public interface ArgumentMapper<I, O> {

    /**
     * Accept's I data from the argument and returns O output
     *
     * @param i The input processed from an argument
     *
     * @return The complex data type that came as a result from this argument
     * @throws ArgumentSyntaxException If the input can not be turned into the desired output
     *          (E.X. an invalid extension name)
     */
    O accept(I i) throws ArgumentSyntaxException;

}
