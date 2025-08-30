package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.ai.EntityAI;
import net.minestom.server.entity.pathfinding.NavigableEntity;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.entity.pathfinding.NavigatorImpl;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.thread.Acquirable;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EntityCreature extends LivingEntity implements NavigableEntity {

    private int removalAnimationDelay = 1000;

    private final EntityAI ai = new EntityAI();
    private final Navigator navigator;

    /**
     * Constructor which allows to specify an UUID. Only use if you know what you are doing!
     */
    public EntityCreature(EntityType entityType, UUID uuid) {
        super(entityType, uuid);
        heal();
        this.navigator = createNavigator();
    }

    public EntityCreature(EntityType entityType) {
        this(entityType, UUID.randomUUID());
    }

    protected Navigator createNavigator() {
        return new NavigatorImpl(this);
    }

    @Override
    public void update(long time) {
        // AI
        ai.tick(time);

        // Path finding
        this.navigator.tick();

        // Fire, item pickup, ...
        super.update(time);
    }

    @Override
    public CompletableFuture<Void> setInstance(Instance instance, Pos spawnPosition) {
        this.navigator.reset();
        return super.setInstance(instance, spawnPosition);
    }

    @Override
    public void kill() {
        super.kill();

        if (removalAnimationDelay > 0) {
            // Needed for proper death animation (wait for it to finish before destroying the entity)
            scheduleRemove(Duration.of(removalAnimationDelay, TimeUnit.MILLISECOND));
        } else {
            // Instant removal without animation playback
            remove();
        }
    }

    /**
     * Gets the kill animation delay before vanishing the entity.
     *
     * @return the removal animation delay in milliseconds, 0 if not any
     */
    public int getRemovalAnimationDelay() {
        return removalAnimationDelay;
    }

    /**
     * Changes the removal animation delay of the entity.
     * <p>
     * Testing shows that 1000 is the minimum value to display the death particles.
     *
     * @param removalAnimationDelay the new removal animation delay in milliseconds, 0 to remove it
     */
    public void setRemovalAnimationDelay(int removalAnimationDelay) {
        this.removalAnimationDelay = removalAnimationDelay;
    }

    public EntityAI getAi() {
        return ai;
    }

    @Override
    public Navigator getNavigator() {
        return navigator;
    }

    /**
     * Calls a {@link EntityAttackEvent} with this entity as the source and {@code target} as the target.
     *
     * @param target    the entity target
     * @param swingHand true to swing the entity main hand, false otherwise
     */
    public void attack(Entity target, boolean swingHand) {
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
    public void attack(Entity target) {
        attack(target, false);
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Experimental
    @Override
    public Acquirable<? extends EntityCreature> acquirable() {
        return (Acquirable<? extends EntityCreature>) super.acquirable();
    }
}
