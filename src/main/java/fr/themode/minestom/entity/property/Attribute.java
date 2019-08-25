package fr.themode.minestom.entity.property;

public enum Attribute {

    MAX_HEALTH("generic.maxHealth", 20),
    FOLLOW_RANGE("generic.followRange", 32),
    KNOCKBACK_RESISTANCE("generic.knockbackResistance", 0),
    MOVEMENT_SPEED("generic.movementSpeed", 0.7f),
    ATTACK_DAMAGE("generic.attackDamage", 2),
    ATTACK_SPEED("generic.attackSpeed", 4),
    FLYING_SPEED("generic.flyingSpeed", 0.4f),
    HORSE_JUMP_STRENGTH("horse.jumpStrength", 0.7f),
    ZOMBIE_SPAWN_REINFORCEMENTS("zombie.spawnReinforcements", 0);

    private String key;
    private float defaultValue;

    Attribute(String key, float defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public float getDefaultValue() {
        return defaultValue;
    }
}
