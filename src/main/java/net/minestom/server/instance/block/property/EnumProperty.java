package net.minestom.server.instance.block.property;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

record EnumProperty<T>(
        String key,
        Function<T, String> valueFunction,
        Function<String, @Nullable T> parseFunction) implements Property<T> {
    @Override
    @Nullable
    public T parse(String value) {
        return parseFunction.apply(value);
    }

    @Override
    public String valueOf(T value) {
        return valueFunction.apply(value);
    }
}
