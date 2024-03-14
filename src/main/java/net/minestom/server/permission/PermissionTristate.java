package net.minestom.server.permission;

import org.jetbrains.annotations.Nullable;

public enum PermissionTristate {
    TRUE(true),
    FALSE(false),
    UNDEFINED(null);

    private final @Nullable Boolean value;

    PermissionTristate(@Nullable Boolean value) {
        this.value = value;
    }

    public @Nullable Boolean getValue() {
        return value;
    }

    public boolean asBoolean(boolean defaultValue) {
        return value != null ? value : defaultValue;
    }

    public boolean asBoolean() {
        return asBoolean(false);
    }

    public static PermissionTristate fromBoolean(Boolean value) {
        if (value == null) return UNDEFINED;
        return value ? TRUE : FALSE;
    }
}