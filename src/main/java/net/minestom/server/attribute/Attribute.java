package net.minestom.server.attribute;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a {@link net.minestom.server.entity.LivingEntity living entity} attribute.
 */
public record Attribute(String key, float defaultValue, float maxValue) {
    private static final Map<String, Attribute> ATTRIBUTES = new ConcurrentHashMap<>();

    public static final Attribute MAX_HEALTH = (new Attribute("generic.max_health", 20, 1024)).register();
    public static final Attribute FOLLOW_RANGE = (new Attribute("generic.follow_range", 32, 2048)).register();
    public static final Attribute KNOCKBACK_RESISTANCE = (new Attribute("generic.knockback_resistance", 0, 1)).register();
    public static final Attribute MOVEMENT_SPEED = (new Attribute("generic.movement_speed", 0.25f, 1024)).register();
    public static final Attribute ATTACK_DAMAGE = (new Attribute("generic.attack_damage", 2, 2048)).register();
    public static final Attribute ATTACK_SPEED = (new Attribute("generic.attack_speed", 4, 1024)).register();
    public static final Attribute FLYING_SPEED = (new Attribute("generic.flying_speed", 0.4f, 1024)).register();
    public static final Attribute ARMOR = (new Attribute("generic.armor", 0, 30)).register();
    public static final Attribute ARMOR_TOUGHNESS = (new Attribute("generic.armor_toughness", 0, 20)).register();
    public static final Attribute ATTACK_KNOCKBACK = (new Attribute("generic.attack_knockback", 0, 5)).register();
    public static final Attribute LUCK = (new Attribute("generic.luck", 0, 1024)).register();
    public static final Attribute HORSE_JUMP_STRENGTH = (new Attribute("horse.jump_strength", 0.7f, 2)).register();
    public static final Attribute ZOMBIE_SPAWN_REINFORCEMENTS = (new Attribute("zombie.spawn_reinforcements", 0, 1)).register();

    public Attribute {
        if (defaultValue > maxValue) {
            throw new IllegalArgumentException("Default value cannot be greater than the maximum allowed");
        }
    }

    /**
     * Register this attribute.
     *
     * @return this attribute
     * @see #fromKey(String)
     * @see #values()
     */
    public @NotNull Attribute register() {
        ATTRIBUTES.put(key, this);
        return this;
    }

    /**
     * Retrieves an attribute by its key.
     *
     * @param key the key of the attribute
     * @return the attribute for the key or null if not any
     */
    public static @Nullable Attribute fromKey(@NotNull String key) {
        return ATTRIBUTES.get(key);
    }

    /**
     * Retrieves all registered attributes.
     *
     * @return an array containing all registered attributes
     */
    public static @NotNull Collection<@NotNull Attribute> values() {
        return ATTRIBUTES.values();
    }
}
