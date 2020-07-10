package net.minestom.server.command.builder;

public interface CommandExecutor<S> {
    void apply(S source, Arguments args);
}