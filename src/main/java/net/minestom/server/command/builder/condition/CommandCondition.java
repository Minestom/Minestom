package net.minestom.server.command.builder.condition;

public interface CommandCondition<S> {
    boolean apply(S source);
}
