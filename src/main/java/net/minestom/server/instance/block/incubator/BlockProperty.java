package net.minestom.server.instance.block.incubator;

import org.jetbrains.annotations.NotNull;

public class BlockProperty<T> {

    private final String name;
    private final T defaultValue;
    private final T[] values;

    @SafeVarargs
    public BlockProperty(@NotNull String name, @NotNull T defaultValue, @NotNull T... values) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.values = values;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull T getDefaultValue() {
        return defaultValue;
    }

    public @NotNull T[] getValues() {
        return values;
    }
}
