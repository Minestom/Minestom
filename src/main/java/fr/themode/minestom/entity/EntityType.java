package fr.themode.minestom.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public enum EntityType {
    ARMOR_STAND(1), // Object
    BAT(3),
    BLAZE(4),
    CAT(6),
    CAVE_SPIDER(7),
    CHICKEN(8),
    COD(9),
    COW(10),
    CREEPER(11),
    DONKEY(12),
    DOLPHIN(13),
    DROWNED(15),
    ELDER_GUARDIAN(16),
    ENDER_DRAGON(18),
    ENDERMAN(19),
    ENDERMITE(20),
    EVOKER(22),
    FOX(27),
    GHAST(28),
    GIANT(29),
    GUARDIAN(30),
    HORSE(31),
    HUSK(32),
    ILLUSIONER(33),
    LLAMA(38),
    MAGMA_CUBE(40),
    MULE(48),
    MOOSHROOM(49),
    OCELOT(50),
    PANDA(52),
    PARROT(53),
    PIG(54),
    PUFFERFISH(55),
    ZOMBIE_PIGMAN(56),
    POLAR_BEAR(57),
    RABBIT(59),
    SALMON(60),
    SHEEP(61),
    SHULKER(62),
    SILVERFISH(64),
    SKELETON(65),
    SKELETON_HORSE(66),
    SLIME(67),
    SNOW_GOLEM(69),
    SPIDER(72),
    SQUID(73),
    STRAY(74),
    TRADER_LLAMA(75),
    TROPICAL_FISH(76),
    TURTLE(77),
    VEX(83),
    VILLAGER(84),
    IRON_GOLEM(85),
    VINDICATOR(86),
    PILLAGER(87),
    WANGERING_TRADER(88),
    WITCH(89),
    WITHER(90),
    WITHER_SKELETON(91),
    WOLF(93),
    ZOMBIE(94),
    ZOMBIE_HORSE(95),
    ZOMBIE_VILLAGER(96),
    PHANTOM(97),
    RAVAGER(98);

    private static Int2ObjectOpenHashMap<EntityType> map = new Int2ObjectOpenHashMap();

    static {
        for (EntityType type : EntityType.values()) {
            map.put(type.id, type);
        }
    }

    private int id;

    EntityType(int id) {
        this.id = id;
    }

    public static EntityType fromId(int id) {
        return map.get(id);
    }

    public int getId() {
        return id;
    }
}
