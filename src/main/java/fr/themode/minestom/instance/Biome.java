package fr.themode.minestom.instance;

import java.util.Arrays;

public enum Biome {

    OCEAN(0),
    PLAINS(1),
    VOID(127);

    private int id;

    Biome(int id) {
        this.id = id;
    }

    public static Biome fromId(int id) {
        return Arrays.stream(values()).filter(customBiome -> customBiome.id == id).findFirst().get();
    }

    public int getId() {
        return id;
    }
}
