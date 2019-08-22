package fr.themode.minestom.item;

import java.util.HashMap;
import java.util.Map;

public enum Material {

    AIR(0),
    STONE(1),
    BOW(525),
    ARROW(526);

    private static Map<Integer, Material> idToMaterial = new HashMap<>();

    static {
        for (Material material : values()) {
            idToMaterial.put(material.id, material);
        }
    }

    private int id;

    Material(int id) {
        this.id = id;
    }

    public static Material fromId(int id) {
        return idToMaterial.get(id);
    }

    public boolean isBlock() {
        return false; // TODO
    }

    public boolean isFood() {
        return false; // TODO
    }

    public int getId() {
        return id;
    }
}
