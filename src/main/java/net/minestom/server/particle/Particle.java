package net.minestom.server.particle;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public enum Particle {

    AMBIENT_ENTITY_EFFECT,
    ANGRY_VILLAGER,
    BARRIER,
    BLOCK,
    BUBBLE,
    CLOUD,
    CRIT,
    DAMAGE_INDICATOR,
    DRAGON_BREATH,
    DRIPPING_LAVA,
    FALLING_LAVA,
    LANDING_LAVA,
    DRIPPING_WATER,
    FALLING_WATER,
    DUST,
    EFFECT,
    ELDER_GUARDIAN,
    ENCHANTED_HIT,
    ENCHANT,
    END_ROD,
    ENTITY_EFFECT,
    EXPLOSION_EMITTER,
    EXPLOSION,
    FALLING_DUST,
    FIREWORK,
    FISHING,
    FLAME,
    FLASH,
    HAPPY_VILLAGER,
    COMPOSTER,
    HEART,
    INSTANT_EFFECT,
    ITEM,
    ITEM_SLIME,
    ITEM_SNOWBALL,
    LARGE_SMOKE,
    LAVA,
    MYCELIUM,
    NOTE,
    POOF,
    PORTAL,
    RAIN,
    SMOKE,
    SNEEZE,
    SPIT,
    SQUID_INK,
    SWEEP_ATTACK,
    TOTEM_OF_UNDYING,
    UNDERWATER,
    SPLASH,
    WITCH,
    BUBBLE_POP,
    CURRENT_DOWN,
    BUBBLE_COLUMN_UP,
    NAUTILUS,
    DOLPHIN,
    CAMPFIRE_COSY_SMOKE,
    CAMPFIRE_SIGNAL_SMOKE,
    DRIPPING_HONEY,
    FALLING_HONEY,
    LANDING_HONEY,
    FALLING_NECTAR;

    private static Int2ObjectOpenHashMap<Particle> map = new Int2ObjectOpenHashMap();

    private int id;

    public static Particle fromId(int id) {
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
