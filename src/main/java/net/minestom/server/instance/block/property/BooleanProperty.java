package net.minestom.server.instance.block.property;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record BooleanProperty(String key) implements Property<Boolean> {
    @Override
    public Boolean typedValueOf(String value) {
        return switch (value) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException(
                    "'" + value + "' is not a valid boolean value for property '" + key + "'");
        };
    }

    @Override
    public String untypedValueOf(Boolean value) {
        return value ? "true" : "false";
    }
}
