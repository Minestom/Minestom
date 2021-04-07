package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.ArgumentMapper;
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
    final ArgumentMapper<I, O> mapper;

    protected ArgumentMap(Argument<I> argument, ArgumentMapper<I, O> mapper) {
        super(argument.getId());

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
}
