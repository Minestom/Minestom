package net.minestom.server.instance.block.property;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record IntegerProperty(String key) implements Property<Integer> {
    @Override
    public Integer typedValueOf(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException _) {
            throw new IllegalArgumentException(
                    "'" + value + "' is not a valid integer value for property '" + key + "'");
        }
    }

    @Override
    public String untypedValueOf(Integer value) {
        return value.toString();
    }
}
