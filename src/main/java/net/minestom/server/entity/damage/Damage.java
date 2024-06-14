package net.minestom.server.entity.damage;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.Taggable;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a type of damage, required when calling {@link LivingEntity#damage(Damage)}
 * and retrieved in {@link net.minestom.server.event.entity.EntityDamageEvent}.
 * <p>
 * This class can be extended if you need to include custom fields and/or methods.
 */
public class Damage implements Taggable {
    private static final DynamicRegistry<DamageType> DAMAGE_TYPE_REGISTRY = MinecraftServer.getDamageTypeRegistry();

    private final DynamicRegistry.Key<DamageType> typeKey;
    private final DamageType type;
    private final Entity source;
    private final Entity attacker;
    private final Point sourcePosition;
    private final TagHandler tagHandler = TagHandler.newHandler();

    private float amount;

    /**
     * Creates a new damage type.
     *
     * @param type the type of this damage
     * @param amount amount of damage
     */
    public Damage(@NotNull DynamicRegistry.Key<DamageType> type, @Nullable Entity source, @Nullable Entity attacker, @Nullable Point sourcePosition, float amount) {
        this.typeKey = type;
        this.type = DAMAGE_TYPE_REGISTRY.get(type);
        Check.argCondition(this.type == null, "Damage type is not registered: {0}", type);
        this.source = source;
        this.attacker = attacker;
        this.sourcePosition = sourcePosition;
        this.amount = amount;
    }

    /**
     * Gets the type of this damage.
     * <p>
     * It does not have to be unique to this object.o
     *
     * @return the damage type
     */
    public @NotNull DynamicRegistry.Key<DamageType> getType() {
        return typeKey;
    }

    /**
     * Gets the "attacker" of the damage.
     * This is the indirect cause of the damage, like the shooter of a projectile, or null if there was none.
     *
     * @return the attacker
     */
    public @Nullable Entity getAttacker() {
        return attacker;
    }

    /**
     * Gets the direct source of the damage.
     * This is the entity that directly causes the damage, like a projectile, or null if there was none.
     *
     * @return the source
     */
    public @Nullable Entity getSource() {
        return source;
    }

    /**
     * Gets the position of the source of the damage, or null if there is none.
     * This may differ from the source entity's position.
     *
     * @return The source position
     */
    public @Nullable Point getSourcePosition() {
        return sourcePosition;
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
        return Component.translatable("death.attack." + type.messageId(), Component.text(killed.getUsername()));
    }

    /**
     * Convenient method to create an {@link EntityProjectileDamage}.
     *
     * @param shooter    the shooter
     * @param projectile the actual projectile
     * @param amount amount of damage
     * @return a new {@link EntityProjectileDamage}
     */
    public static @NotNull Damage fromProjectile(@Nullable Entity shooter, @NotNull Entity projectile, float amount) {
        return new EntityProjectileDamage(shooter, projectile, amount);
    }

    /**
     * Convenient method to create an {@link EntityDamage}.
     *
     * @param player the player damager
     * @param amount amount of damage
     * @return a new {@link EntityDamage}
     */
    public static @NotNull EntityDamage fromPlayer(@NotNull Player player, float amount) {
        return new EntityDamage(player, amount);
    }

    /**
     * Convenient method to create an {@link EntityDamage}.
     *
     * @param entity the entity damager
     * @param amount amount of damage
     * @return a new {@link EntityDamage}
     */
    public static @NotNull EntityDamage fromEntity(@NotNull Entity entity, float amount) {
        return new EntityDamage(entity, amount);
    }

    public static @NotNull PositionalDamage fromPosition(@NotNull DynamicRegistry.Key<DamageType> type, @NotNull Point sourcePosition, float amount) {
        return new PositionalDamage(type, sourcePosition, amount);
    }

    /**
     * Builds the text sent to a player in his death screen.
     *
     * @param killed the player who has been killed
     * @return the death screen text, null to do not send anything
     */
    public @Nullable Component buildDeathScreenText(@NotNull Player killed) {
        return Component.translatable("death.attack." + type.messageId());
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
        if (DamageType.ON_FIRE.equals(typeKey)) return SoundEvent.ENTITY_PLAYER_HURT_ON_FIRE;
        return SoundEvent.ENTITY_PLAYER_HURT;
    }

    @Override
    public @NotNull TagHandler tagHandler() {
        return tagHandler;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
