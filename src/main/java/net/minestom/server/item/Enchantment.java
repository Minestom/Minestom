package net.minestom.server.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public enum Enchantment {

    PROTECTION,
    FIRE_PROTECTION,
    FEATHER_FALLING,
    BLAST_PROTECTION,
    PROJECTILE_PROTECTION,
    RESPIRATION,
    AQUA_AFFINITY,
    THORNS,
    DEPTH_STRIDER,
    FROST_WALKER,
    BINDING_CURSE,
    SOUL_SPEED,
    SHARPNESS,
    SMITE,
    BANE_OF_ARTHROPODS,
    KNOCKBACK,
    FIRE_ASPECT,
    LOOTING,
    SWEEPING,
    EFFICIENCY,
    SILK_TOUCH,
    UNBREAKING,
    FORTUNE,
    POWER,
    PUNCH,
    FLAME,
    INFINITY,
    LUCK_OF_THE_SEA,
    LURE,
    LOYALTY,
    IMPALING,
    RIPTIDE,
    CHANNELING,
    MULTISHOT,
    QUICK_CHARGE,
    PIERCING,
    MENDING,
    VANISHING_CURSE;

    private static Int2ObjectOpenHashMap<Enchantment> map = new Int2ObjectOpenHashMap();

    private int id;

    public static Enchantment fromId(int id) {
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
