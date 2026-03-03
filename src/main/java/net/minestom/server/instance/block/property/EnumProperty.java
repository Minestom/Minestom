package net.minestom.server.instance.block.property;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

record EnumProperty<T extends PropertyEnum>(
        String key,
        Function<String, @Nullable T> enumTypedValueOf) implements Property<T> {
    @Override
    @Nullable
    public T parse(String value) {
        return enumTypedValueOf.apply(value);
    }

    @Override
    public String valueOf(T value) {
        return value.value();
    }
}
