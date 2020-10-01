package net.minestom.server.attribute;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Attribute {

    MAX_HEALTH("generic.max_health", 20, 1024),
    FOLLOW_RANGE("generic.follow_range", 32, 2048),
    KNOCKBACK_RESISTANCE("generic.knockback_resistance", 0, 1),
    MOVEMENT_SPEED("generic.movement_speed", 0.25f, 1024),
    ATTACK_DAMAGE("generic.attack_damage", 2, 2048),
    ATTACK_SPEED("generic.attack_speed", 4, 1024),
    FLYING_SPEED("generic.flying_speed", 0.4f, 1024),
    ARMOR("generic.armor", 0, 30),
    ARMOR_TOUGHNESS("generic.armor_toughness", 0, 20),
    ATTACK_KNOCKBACK("generic.attack_knockback", 0, 5),
    LUCK("generic.luck", 0, 1024),
    HORSE_JUMP_STRENGTH("horse.jump_strength", 0.7f, 2),
    ZOMBIE_SPAWN_REINFORCEMENTS("zombie.spawn_reinforcements", 0, 1);

    private final String key;
    private final float defaultValue;
    private final float maxVanillaValue;

    Attribute(String key, float defaultValue, float maxVanillaValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.maxVanillaValue = maxVanillaValue;
    }

    @NotNull
    public String getKey() {
        return key;
    }

    public float getDefaultValue() {
        return defaultValue;
    }

    public float getMaxVanillaValue() {
        return maxVanillaValue;
    }

    @Nullable
    public static Attribute fromKey(@NotNull String key) {
        for (Attribute attribute : values()) {
            if (attribute.getKey().equals(key))
                return attribute;
        }
        return null;
    }
}
