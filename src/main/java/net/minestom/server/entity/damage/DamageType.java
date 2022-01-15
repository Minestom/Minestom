package net.minestom.server.entity.damage;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

/**
 * Represents a type of damage, required when calling {@link LivingEntity#damage(DamageType, float)}
 * and retrieved in {@link net.minestom.server.event.entity.EntityDamageEvent}.
 * <p>
 * This class can be extended if you need to include custom fields and/or methods.
 */
public class DamageType implements TagHandler {

    public static final DamageType VOID = new DamageType("attack.outOfWorld");
    public static final DamageType GRAVITY = new DamageType("attack.fall");
    public static final DamageType ON_FIRE = new DamageType("attack.onFire") {
        @Override
        protected SoundEvent getPlayerSound(@NotNull Player player) {
            return SoundEvent.ENTITY_PLAYER_HURT_ON_FIRE;
        }
    };
    private final String identifier;
    private final Object nbtLock = new Object();
    private final MutableNBTCompound nbt = new MutableNBTCompound();

    /**
     * Creates a new damage type.
     *
     * @param identifier the identifier of this damage type,
     *                   does not need to be unique
     */
    public DamageType(@NotNull String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the identifier of this damage type.
     * <p>
     * It does not have to be unique to this object.o
     *
     * @return the damage type identifier
     */
    public @NotNull String getIdentifier() {
        return identifier;
    }

    /**
     * Builds the death message linked to this damage type.
     * <p>
     * Used in {@link Player#kill()} to broadcast the proper message.
     *
     * @param killed the player who has been killed
     * @return the death message, null to do not send anything
     */
    public @Nullable Component buildDeathMessage(@NotNull Player killed) {
        return Component.translatable("death." + identifier, Component.text(killed.getUsername()));
    }

    /**
     * Convenient method to create an {@link EntityProjectileDamage}.
     *
     * @param shooter    the shooter
     * @param projectile the actual projectile
     * @return a new {@link EntityProjectileDamage}
     */
    public static @NotNull DamageType fromProjectile(@Nullable Entity shooter, @NotNull Entity projectile) {
        return new EntityProjectileDamage(shooter, projectile);
    }

    /**
     * Convenient method to create an {@link EntityDamage}.
     *
     * @param player the player damager
     * @return a new {@link EntityDamage}
     */
    public static @NotNull EntityDamage fromPlayer(@NotNull Player player) {
        return new EntityDamage(player);
    }

    /**
     * Convenient method to create an {@link EntityDamage}.
     *
     * @param entity the entity damager
     * @return a new {@link EntityDamage}
     */
    public static @NotNull EntityDamage fromEntity(@NotNull Entity entity) {
        return new EntityDamage(entity);
    }

    /**
     * Builds the text sent to a player in his death screen.
     *
     * @param killed the player who has been killed
     * @return the death screen text, null to do not send anything
     */
    public @Nullable Component buildDeathScreenText(@NotNull Player killed) {
        return Component.translatable("death." + identifier);
    }

    /**
     * Sound event to play when the given entity is hit by this damage. Possible to return null if no sound should be played
     *
     * @param entity the entity hit by this damage
     * @return the sound to play when the given entity is hurt by this damage type. Can be null if no sound should play
     */
    public @Nullable SoundEvent getSound(@NotNull LivingEntity entity) {
        if (entity instanceof Player) {
            return getPlayerSound((Player) entity);
        }
        return getGenericSound(entity);
    }

    protected SoundEvent getGenericSound(@NotNull LivingEntity entity) {
        return SoundEvent.ENTITY_GENERIC_HURT;
    }

    protected SoundEvent getPlayerSound(@NotNull Player player) {
        return SoundEvent.ENTITY_PLAYER_HURT;
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
        synchronized (nbtLock) {
            return tag.read(nbt);
        }
    }

    @Override
    public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        synchronized (nbtLock) {
            tag.write(nbt, value);
        }
    }
}
