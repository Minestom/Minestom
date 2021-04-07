package net.minestom.server.command.builder;

import net.minestom.server.command.builder.exception.ArgumentSyntaxException;

@FunctionalInterface
public interface ArgumentMapper<I, O> {

    O accept(I i) throws ArgumentSyntaxException;

}
