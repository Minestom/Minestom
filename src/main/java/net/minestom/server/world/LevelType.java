package net.minestom.server.world;

public enum LevelType {

    DEFAULT("default"),
    FLAT("flat"),
    LARGE_BIOMES("largeBiomes"),
    AMPLIFIED("amplified"),
    DEFAULT_1_1("default_1_1");

    private String type;

    LevelType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
