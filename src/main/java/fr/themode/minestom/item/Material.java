package fr.themode.minestom.item;

import java.util.HashMap;
import java.util.Map;

public enum Material {

    AIR(0, 0),
    STONE(1, 1),
    BOW(525, 0),
    ARROW(526, 0),
    DIAMOND_SWORD(541, 0);

    private static Map<Integer, Material> idToMaterial = new HashMap<>();

    static {
        for (Material material : values()) {
            idToMaterial.put(material.id, material);
        }
    }

    private int id;
    private int blockId;

    Material(int id, int blockId) {
        this.id = id;
        this.blockId = blockId;
    }

    public static Material fromId(int id) {
        return idToMaterial.get(id);
    }

    public boolean isBlock() {
        return blockId != 0;
    }

    public boolean isFood() {
        return false; // TODO
    }

    public int getId() {
        return id;
    }
}
