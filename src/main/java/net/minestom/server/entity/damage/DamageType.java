package net.minestom.server.entity.damage;

import club.thectm.minecraft.text.TextBuilder;
import club.thectm.minecraft.text.TextObject;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.Sound;

/**
 * Represents a type of damage
 */
public class DamageType {

    public static final DamageType VOID = new DamageType("attack.outOfWorld");
    public static final DamageType GRAVITY = new DamageType("attack.fall");
    public static final DamageType ON_FIRE = new DamageType("attack.onFire") {
        @Override
        protected Sound getPlayerSound(Player player) {
            return Sound.ENTITY_PLAYER_HURT_ON_FIRE;
        }
    };
    private final String identifier;

    public DamageType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public static DamageType fromProjectile(Entity shooter, Entity projectile) {
        return new EntityProjectileDamage(shooter, projectile);
    }

    public TextObject buildChatMessage(Player killed) {
        return TextBuilder.ofTranslation("death."+identifier, TextBuilder.of(killed.getUsername()).build()).build();
    }

    public static DamageType fromPlayer(Player player) {
        return new EntityDamage(player);
    }

    public TextObject buildDeathScreenMessage(Player killed) {
        return buildChatMessage(killed);
    }

    /**
     * Sound event to play when the given entity is hit by this damage. Possible to return null if no sound should be played
     * @param entity the entity hit by this damage
     * @return the sound to play when the given entity is hurt by this damage type. Can be null if no sound should play
     */
    public Sound getSound(LivingEntity entity) {
        if(entity instanceof Player) {
            return getPlayerSound((Player)entity);
        }
        return getGenericSound(entity);
    }

    protected Sound getGenericSound(LivingEntity entity) {
        return Sound.ENTITY_GENERIC_HURT;
    }

    protected Sound getPlayerSound(Player player) {
        return Sound.ENTITY_PLAYER_HURT;
    }
}
