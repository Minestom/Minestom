package fr.themode.minestom.world;

import java.util.Arrays;

public enum CustomBiome {

    OCEAN(0),
    PLAINS(1),
    VOID(127);

    private int id;

    CustomBiome(int id) {
        this.id = id;
    }

    public static CustomBiome fromId(int id) {
        return Arrays.stream(values()).filter(customBiome -> customBiome.id == id).findFirst().get();
    }

    public int getId() {
        return id;
    }
}
