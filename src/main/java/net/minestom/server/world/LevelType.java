package net.minestom.server.world;

import org.jetbrains.annotations.Nullable;

public enum LevelType {

    DEFAULT("default"),
    FLAT("flat"),
    LARGE_BIOMES("largeBiomes"),
    AMPLIFIED("amplified"),
    DEFAULT_1_1("default_1_1");

    private final String type;

    LevelType(@Nullable String type) {
        this.type = type;
    }

    @Nullable
    public String getType() {
        return type;
    }
}
