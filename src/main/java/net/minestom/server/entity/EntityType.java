package net.minestom.server.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public enum EntityType {

    AREA_EFFECT_CLOUD,
    ARMOR_STAND,
    ARROW,
    BAT,
    BEE,
    BLAZE,
    BOAT,
    CAT,
    CAVE_SPIDER,
    CHICKEN,
    COD,
    COW,
    CREEPER,
    DOLPHIN,
    DONKEY,
    DRAGON_FIREBALL,
    DROWNED,
    ELDER_GUARDIAN,
    END_CRYSTAL,
    ENDER_DRAGON,
    ENDERMAN,
    ENDERMITE,
    EVOKER,
    EVOKER_FANGS,
    EXPERIENCE_ORB,
    EYE_OF_ENDER,
    FALLING_BLOCK,
    FIREWORK_ROCKET,
    FOX,
    GHAST,
    GIANT,
    GUARDIAN,
    HOGLIN,
    HORSE,
    HUSK,
    ILLUSIONER,
    IRON_GOLEM,
    ITEM,
    ITEM_FRAME,
    FIREBALL,
    LEASH_KNOT,
    LIGHTNING_BOLT,
    LLAMA,
    LLAMA_SPIT,
    MAGMA_CUBE,
    MINECART,
    CHEST_MINECART,
    COMMAND_BLOCK_MINECART,
    FURNACE_MINECART,
    HOPPER_MINECART,
    SPAWNER_MINECART,
    TNT_MINECART,
    MULE,
    MOOSHROOM,
    OCELOT,
    PAINTING,
    PANDA,
    PARROT,
    PHANTOM,
    PIG,
    PIGLIN,
    PILLAGER,
    POLAR_BEAR,
    TNT,
    PUFFERFISH,
    RABBIT,
    RAVAGER,
    SALMON,
    SHEEP,
    SHULKER,
    SHULKER_BULLET,
    SILVERFISH,
    SKELETON,
    SKELETON_HORSE,
    SLIME,
    SMALL_FIREBALL,
    SNOW_GOLEM,
    SNOWBALL,
    SPECTRAL_ARROW,
    SPIDER,
    SQUID,
    STRAY,
    STRIDER,
    EGG,
    ENDER_PEARL,
    EXPERIENCE_BOTTLE,
    POTION,
    TRIDENT,
    TRADER_LLAMA,
    TROPICAL_FISH,
    TURTLE,
    VEX,
    VILLAGER,
    VINDICATOR,
    WANDERING_TRADER,
    WITCH,
    WITHER,
    WITHER_SKELETON,
    WITHER_SKULL,
    WOLF,
    ZOGLIN,
    ZOMBIE,
    ZOMBIE_HORSE,
    ZOMBIE_VILLAGER,
    ZOMBIFIED_PIGLIN,
    PLAYER,
    FISHING_BOBBER;

    private static Int2ObjectOpenHashMap<EntityType> map = new Int2ObjectOpenHashMap();

    private int id;

    public void setIdentifier(int id) {
        this.id = id;

        map.put(id, this);
    }

    public static EntityType fromId(int id) {
        return map.get(id);
    }

    public int getId() {
        return id;
    }
}
