package net.minestom.server.entity.damage;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.chat.RichMessage;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataContainer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.Sound;

/**
 * Represents a type of damage, required when calling {@link LivingEntity#damage(DamageType, float)}
 * and retrieved in {@link net.minestom.server.event.entity.EntityDamageEvent}.
 * <p>
 * This class can be extended if you need to include custom fields and/or methods.
 * Be aware that this class implements {@link DataContainer}
 * so you can add your own data to an already existing damage type without any wrapper.
 */
public class DamageType implements DataContainer {

    public static final DamageType VOID = new DamageType("attack.outOfWorld");
    public static final DamageType GRAVITY = new DamageType("attack.fall");
    public static final DamageType ON_FIRE = new DamageType("attack.onFire") {
        @Override
        protected Sound getPlayerSound(Player player) {
            return Sound.ENTITY_PLAYER_HURT_ON_FIRE;
        }
    };
    private final String identifier;
    private Data data;

    /**
     * Creates a new damage type.
     *
     * @param identifier the identifier of this damage type,
     *                   does not need to be unique
     */
    public DamageType(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the identifier of this damage type.
     * <p>
     * It does not have to be unique to this object.o
     *
     * @return the damage type identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Builds the death message linked to this damage type.
     * <p>
     * Used in {@link Player#kill()} to broadcast the proper message.
     *
     * @param killed the player who has been killed
     * @return the death message, null to do not send anything.
     * Can be for instance, of type {@link ColoredText} or {@link RichMessage}.
     */
    public JsonMessage buildDeathMessage(Player killed) {
        return ColoredText.of("{@death." + identifier + "," + killed.getUsername() + "}");
    }

    /**
     * Convenient method to create an {@link EntityProjectileDamage}.
     *
     * @param shooter    the shooter
     * @param projectile the actual projectile
     * @return a new {@link EntityProjectileDamage}
     */
    public static DamageType fromProjectile(Entity shooter, Entity projectile) {
        return new EntityProjectileDamage(shooter, projectile);
    }

    /**
     * Convenient method to create an {@link EntityDamage}.
     *
     * @param player the player damager
     * @return a new {@link EntityDamage}
     */
    public static EntityDamage fromPlayer(Player player) {
        return new EntityDamage(player);
    }

    /**
     * Convenient method to create an {@link EntityDamage}.
     *
     * @param entity the entity damager
     * @return a new {@link EntityDamage}
     */
    public static EntityDamage fromEntity(Entity entity) {
        return new EntityDamage(entity);
    }

    /**
     * Builds the text sent to a player in his death screen.
     *
     * @param killed the player who has been killed
     * @return the death screen text, null to do not send anything
     */
    public ColoredText buildDeathScreenText(Player killed) {
        return ColoredText.of("{@death." + identifier + "}");
    }

    /**
     * Sound event to play when the given entity is hit by this damage. Possible to return null if no sound should be played
     *
     * @param entity the entity hit by this damage
     * @return the sound to play when the given entity is hurt by this damage type. Can be null if no sound should play
     */
    public Sound getSound(LivingEntity entity) {
        if (entity instanceof Player) {
            return getPlayerSound((Player) entity);
        }
        return getGenericSound(entity);
    }

    protected Sound getGenericSound(LivingEntity entity) {
        return Sound.ENTITY_GENERIC_HURT;
    }

    protected Sound getPlayerSound(Player player) {
        return Sound.ENTITY_PLAYER_HURT;
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public void setData(Data data) {
        this.data = data;
    }
}
