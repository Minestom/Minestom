package net.minestom.server.entity;

import net.minestom.server.entity.ai.EntityAI;
import net.minestom.server.entity.ai.EntityAIGroup;
import net.minestom.server.entity.features.EntityFeatures;
import net.minestom.server.entity.pathfinding.NavigableEntity;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

/**
 * @deprecated Use raw {@link Entity} with {@link EntityFeatures#LIVING}, {@link EntityFeatures#AI} instead.
 */
@Deprecated
public class EntityCreature extends LivingEntity implements NavigableEntity, EntityAI {

    /**
     * Constructor which allows to specify an UUID. Only use if you know what you are doing!
     */
    public EntityCreature(@NotNull EntityType entityType, @NotNull UUID uuid) {
        super(entityType, uuid);
        enableFeature(EntityFeatures.AI);
    }

    public EntityCreature(@NotNull EntityType entityType) {
        this(entityType, UUID.randomUUID());
    }

    @Override
    public Collection<EntityAIGroup> getAIGroups() {
        return getFeature(EntityFeatures.AI).getAIGroups();
    }

    /**
     * Gets the entity target.
     *
     * @return the entity target, can be null if not any
     */
    @Nullable
    public Entity getTarget() {
        return getFeature(EntityFeatures.AI).getTarget();
    }

    /**
     * Changes the entity target.
     *
     * @param target the new entity target, null to remove
     */
    public void setTarget(@Nullable Entity target) {
        getFeature(EntityFeatures.AI).setTarget(target);
    }

    @NotNull
    @Override
    public Navigator getNavigator() {
        return getFeature(EntityFeatures.NAVIGABLE).getNavigator();
    }

    /**
     * Calls a {@link EntityAttackEvent} with this entity as the source and {@code target} as the target.
     *
     * @param target    the entity target
     * @param swingHand true to swing the entity main hand, false otherwise
     */
    public void attack(@NotNull Entity target, boolean swingHand) {
        if (swingHand)
            swingMainHand();
        EntityAttackEvent attackEvent = new EntityAttackEvent(this, target);
        EventDispatcher.call(attackEvent);
    }

    /**
     * Calls a {@link EntityAttackEvent} with this entity as the source and {@code target} as the target.
     * <p>
     * This does not trigger the hand animation.
     *
     * @param target the entity target
     */
    public void attack(@NotNull Entity target) {
        attack(target, false);
    }

}
