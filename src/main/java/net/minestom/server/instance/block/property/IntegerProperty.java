package net.minestom.server.instance.block.property;

import org.jetbrains.annotations.Nullable;

record IntegerProperty(String key) implements Property<Integer> {
    @Override
    @Nullable
    public Integer parse(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException _) {
            return null;
        }
    }

    @Override
    public String valueOf(Integer value) {
        return value.toString();
    }
}
