package net.minestom.server.entity.property;

public enum Attribute {

    MAX_HEALTH("generic.maxHealth", 20, 1024),
    FOLLOW_RANGE("generic.followRange", 32, 2048),
    KNOCKBACK_RESISTANCE("generic.knockbackResistance", 0, 1),
    MOVEMENT_SPEED("generic.movementSpeed", 0.7f, 1024),
    ATTACK_DAMAGE("generic.attackDamage", 2, 2048),
    ATTACK_SPEED("generic.attackSpeed", 4, 1024),
    FLYING_SPEED("generic.flyingSpeed", 0.4f, 1024),
    ARMOR("generic.armor", 0, 30),
    ARMOR_TOUGHNESS("generic.armorToughness", 0, 20),
    ATTACK_KNOCKBACK("generic.attackKnockback", 0, 5),
    LUCK("generic.luck", 0, 1024),
    HORSE_JUMP_STRENGTH("horse.jumpStrength", 0.7f, 2),
    ZOMBIE_SPAWN_REINFORCEMENTS("zombie.spawnReinforcements", 0, 1);

    private String key;
    private float defaultValue;
    private float maxVanillaValue;

    Attribute(String key, float defaultValue, float maxVanillaValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.maxVanillaValue = maxVanillaValue;
    }

    public String getKey() {
        return key;
    }

    public float getDefaultValue() {
        return defaultValue;
    }

    public float getMaxVanillaValue() {
        return maxVanillaValue;
    }

    public static Attribute fromKey(String key) {
        for (Attribute attribute : values()) {
            if (attribute.getKey().equals(key))
                return attribute;
        }
        return null;
    }
}
