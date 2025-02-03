package net.minestom.server.entity;

import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ai.AIGoal;
import net.minestom.server.entity.ai.EntityAI;
import net.minestom.server.entity.ai.EntityAIGroup;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.entity.pathfinding.PathWalker;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.thread.Acquirable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EntityCreature extends Entity implements PathWalker, EntityAI {

    private EntityAIGroup aiGroup = new EntityAIGroup();

    private final Navigator navigator = new Navigator(this);

    private Entity target;

    /**
     * Constructor which allows to specify an UUID. Only use if you know what you are doing!
     */
    public EntityCreature(@NotNull EntityType entityType, @NotNull UUID uuid) {
        super(entityType, uuid);
    }

    public EntityCreature(@NotNull EntityType entityType) {
        this(entityType, UUID.randomUUID());
    }

    @Override
    public void update(long time) {
        // AI
        aiTick(time);

        // Path finding
        this.navigator.tick();

        // Fire, item pickup, ...
        super.update(time);
    }

    @Override
    public CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos spawnPosition) {
        this.navigator.reset();
        return super.setInstance(instance, spawnPosition);
    }

    @Override
    public @NotNull EntityAIGroup getAIGroup() {
        return aiGroup;
    }

    @Override
    public void setAIGroup(@NotNull EntityAIGroup aiGroup) {
        this.aiGroup = aiGroup;
        // Update entity creatures in AI goal
        // TODO: Is this needed?
        for (AIGoal aiGoal : aiGroup.getGoalSelectors()) {
            aiGoal.setEntityCreature(this);
        }
    }

    /**
     * Gets the entity target.
     *
     * @return the entity target, can be null if not any
     */
    @Nullable
    public Entity getTarget() {
        return target;
    }

    /**
     * Changes the entity target.
     *
     * @param target the new entity target, null to remove
     */
    public void setTarget(@Nullable Entity target) {
        this.target = target;
    }

    @NotNull
    public Navigator getNavigator() {
        return navigator;
    }

    /**
     * Calls a {@link EntityAttackEvent} with this entity as the source and {@code target} as the target.
     *
     * @param target    the entity target
     * @param swingHand true to swing the entity main hand, false otherwise
     */
    public void attack(@NotNull Entity target, boolean swingHand) {
        if (swingHand && this.getEntityType().registry().spawnType() == EntitySpawnType.LIVING)
            sendPacketsToViewers(new EntityAnimationPacket(this.getEntityId(), EntityAnimationPacket.Animation.SWING_MAIN_ARM));
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

    @SuppressWarnings("unchecked")
    @ApiStatus.Experimental
    @Override
    public @NotNull Acquirable<? extends EntityCreature> acquirable() {
        return (Acquirable<? extends EntityCreature>) super.acquirable();
    }

    @Override
    public void updateNewPosition(@NotNull Vec speed, float yaw, float pitch) {
        final var physicsResult = CollisionUtils.handlePhysics(this, speed);
        this.refreshPosition(Pos.fromPoint(physicsResult.newPosition()).withView(yaw, pitch));
    }
}
