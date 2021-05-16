package net.minestom.server.instance.block.incubator;

import org.jetbrains.annotations.NotNull;

public class BlockProperty<T> {

    private final String name;
    private final T defaultValue;
    private final T[] possibleValues;

    @SafeVarargs
    public BlockProperty(@NotNull String name, @NotNull T defaultValue, @NotNull T... possibleValues) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.possibleValues = possibleValues;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull T getDefaultValue() {
        return defaultValue;
    }

    public @NotNull T[] getPossibleValues() {
        return possibleValues;
    }
}
