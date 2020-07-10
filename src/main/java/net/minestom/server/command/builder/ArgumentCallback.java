package net.minestom.server.command.builder;

public interface ArgumentCallback<S> {
    void apply(S source, String value, int error);
}
