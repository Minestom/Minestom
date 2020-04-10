package fr.themode.minestom.entity;

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
    DONKEY,
    DOLPHIN,
    DRAGON_FIREBALL,
    DROWNED,
    ELDER_GUARDIAN,
    END_CRYSTAL,
    ENDER_DRAGON,
    ENDERMAN,
    ENDERMITE,
    EVOKER_FANGS,
    EVOKER,
    EXPERIENCE_ORB,
    EYE_OF_ENDER,
    FALLING_BLOCK,
    FIREWORK_ROCKET,
    FOX,
    GHAST,
    GIANT,
    GUARDIAN,
    HORSE,
    HUSK,
    ILLUSIONER,
    ITEM,
    ITEM_FRAME,
    FIREBALL,
    LEASH_KNOT,
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
    PIG,
    PUFFERFISH,
    ZOMBIE_PIGMAN,
    POLAR_BEAR,
    TNT,
    RABBIT,
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
    TRADER_LLAMA,
    TROPICAL_FISH,
    TURTLE,
    EGG,
    ENDER_PEARL,
    EXPERIENCE_BOTTLE,
    POTION,
    TRIDENT,
    VEX,
    VILLAGER,
    IRON_GOLEM,
    VINDICATOR,
    PILLAGER,
    WANDERING_TRADER,
    WITCH,
    WITHER,
    WITHER_SKELETON,
    WITHER_SKULL,
    WOLF,
    ZOMBIE,
    ZOMBIE_HORSE,
    ZOMBIE_VILLAGER,
    PHANTOM,
    RAVAGER,
    LIGHTNING_BOLT,
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
