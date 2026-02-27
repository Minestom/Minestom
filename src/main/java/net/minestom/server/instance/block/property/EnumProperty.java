package net.minestom.server.instance.block.property;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@ApiStatus.Internal
public record EnumProperty<T extends PropertyEnum>(
        String key,
        Function<String, @Nullable T> enumTypedValueOf) implements Property<T> {
    @Override
    public T typedValueOf(String value) {
        final T typed = enumTypedValueOf.apply(value);
        if (typed == null) {
            throw new IllegalArgumentException(
                    "'" + value + "' is not a valid enum value for property '" + key + "'");
        }
        return typed;
    }

    @Override
    public String untypedValueOf(T value) {
        return value.untypedValue();
    }
}
