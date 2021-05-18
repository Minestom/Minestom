package net.minestom.server.instance.block;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class BlockProperty<T> {

    private final String name;
    private final List<T> possibleValues;

    @SafeVarargs
    public BlockProperty(@NotNull String name, @NotNull T... possibleValues) {
        this.name = name;
        this.possibleValues = Arrays.asList(possibleValues);
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull List<T> getPossibleValues() {
        return possibleValues;
    }
}
