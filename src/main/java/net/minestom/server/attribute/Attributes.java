package net.minestom.server.attribute;

/**
 * The Minecraft, vanilla, standards attributes.
 */
public final class Attributes {

    public static final Attribute MAX_HEALTH = (new Attribute("generic.max_health", true, 20, 1024)).register();
    public static final Attribute FOLLOW_RANGE = (new Attribute("generic.follow_range", true, 32, 2048)).register();
    public static final Attribute KNOCKBACK_RESISTANCE = (new Attribute("generic.knockback_resistance", true, 0, 1)).register();
    public static final Attribute MOVEMENT_SPEED = (new Attribute("generic.movement_speed", true, 0.25f, 1024)).register();
    public static final Attribute ATTACK_DAMAGE = (new Attribute("generic.attack_damage", true, 2, 2048)).register();
    public static final Attribute ATTACK_SPEED = (new Attribute("generic.attack_speed", true, 4, 1024)).register();
    public static final Attribute FLYING_SPEED = (new Attribute("generic.flying_speed", true, 0.4f, 1024)).register();
    public static final Attribute ARMOR = (new Attribute("generic.armor", true, 0, 30)).register();
    public static final Attribute ARMOR_TOUGHNESS = (new Attribute("generic.armor_toughness", true, 0, 20)).register();
    public static final Attribute ATTACK_KNOCKBACK = (new Attribute("generic.attack_knockback", true, 0, 5)).register();
    public static final Attribute LUCK = (new Attribute("generic.luck", true, 0, 1024)).register();
    public static final Attribute HORSE_JUMP_STRENGTH = (new Attribute("horse.jump_strength", true, 0.7f, 2)).register();
    public static final Attribute ZOMBIE_SPAWN_REINFORCEMENTS = (new Attribute("zombie.spawn_reinforcements", true, 0, 1)).register();

    private Attributes() throws IllegalAccessException {
        throw new IllegalAccessException("Cannot instantiate a static class");
    }

    protected static void init() {
        // Empty, here to register all the vanilla attributes
    }
}
