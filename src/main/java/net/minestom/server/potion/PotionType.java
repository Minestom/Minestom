package net.minestom.server.potion;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public enum PotionType {

    EMPTY,
    WATER,
    MUNDANE,
    THICK,
    AWKWARD,
    NIGHT_VISION,
    LONG_NIGHT_VISION,
    INVISIBILITY,
    LONG_INVISIBILITY,
    LEAPING,
    LONG_LEAPING,
    STRONG_LEAPING,
    FIRE_RESISTANCE,
    LONG_FIRE_RESISTANCE,
    SWIFTNESS,
    LONG_SWIFTNESS,
    STRONG_SWIFTNESS,
    SLOWNESS,
    LONG_SLOWNESS,
    STRONG_SLOWNESS,
    TURTLE_MASTER,
    LONG_TURTLE_MASTER,
    STRONG_TURTLE_MASTER,
    WATER_BREATHING,
    LONG_WATER_BREATHING,
    HEALING,
    STRONG_HEALING,
    HARMING,
    STRONG_HARMING,
    POISON,
    LONG_POISON,
    STRONG_POISON,
    REGENERATION,
    LONG_REGENERATION,
    STRONG_REGENERATION,
    STRENGTH,
    LONG_STRENGTH,
    STRONG_STRENGTH,
    WEAKNESS,
    LONG_WEAKNESS,
    LUCK,
    SLOW_FALLING,
    LONG_SLOW_FALLING;

    private static Int2ObjectOpenHashMap<PotionType> map = new Int2ObjectOpenHashMap();

    private int id;

    public static PotionType fromId(int id) {
        return map.get(id);
    }

    public void setIdentifier(int id) {
        this.id = id;

        map.put(id, this);
    }

    public int getId() {
        return id;
    }
}
