package net.minestom.server.instance.block.property;

import org.jetbrains.annotations.Nullable;

record BooleanProperty(String key) implements Property<Boolean> {
    @Override
    @Nullable
    public Boolean parse(String value) {
        return switch (value) {
            case "true" -> true;
            case "false" -> false;
            default -> null;
        };
    }

    @Override
    public String valueOf(Boolean value) {
        return value ? "true" : "false";
    }
}
