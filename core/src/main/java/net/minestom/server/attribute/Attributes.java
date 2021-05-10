package net.minestom.server.attribute;

/**
 * The Minecraft, vanilla, standards attributes.
 *
 * @deprecated use the constants in {@link Attribute}
 */
@Deprecated
public final class Attributes {

    public static final Attribute MAX_HEALTH = Attribute.MAX_HEALTH;
    public static final Attribute FOLLOW_RANGE = Attribute.FOLLOW_RANGE;
    public static final Attribute KNOCKBACK_RESISTANCE = Attribute.KNOCKBACK_RESISTANCE;
    public static final Attribute MOVEMENT_SPEED = Attribute.MOVEMENT_SPEED;
    public static final Attribute ATTACK_DAMAGE = Attribute.ATTACK_DAMAGE;
    public static final Attribute ATTACK_SPEED = Attribute.ATTACK_SPEED;
    public static final Attribute FLYING_SPEED = Attribute.FLYING_SPEED;
    public static final Attribute ARMOR = Attribute.ARMOR;
    public static final Attribute ARMOR_TOUGHNESS = Attribute.ARMOR_TOUGHNESS;
    public static final Attribute ATTACK_KNOCKBACK = Attribute.ATTACK_KNOCKBACK;
    public static final Attribute LUCK = Attribute.LUCK;
    public static final Attribute HORSE_JUMP_STRENGTH = Attribute.HORSE_JUMP_STRENGTH;
    public static final Attribute ZOMBIE_SPAWN_REINFORCEMENTS = Attribute.ZOMBIE_SPAWN_REINFORCEMENTS;

    private Attributes() throws IllegalAccessException {
        throw new IllegalAccessException("Cannot instantiate a static class");
    }

    protected static void init() {
        // Empty, here to register all the vanilla attributes
    }
}
