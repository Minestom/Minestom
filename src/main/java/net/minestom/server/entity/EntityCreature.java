package net.minestom.server.entity;

import com.extollit.gaming.ai.path.HydrazinePathFinder;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.ai.EntityAI;
import net.minestom.server.entity.ai.EntityAIGroup;
import net.minestom.server.entity.pathfinding.NavigableEntity;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;

public class EntityCreature extends LivingEntity implements NavigableEntity, EntityAI {

    private int removalAnimationDelay = 1000;

    private final Set<EntityAIGroup> aiGroups = new CopyOnWriteArraySet<>();

    private final Navigator navigator = new Navigator(this);

    private Entity target;

    /**
     * Constructor which allows to specify an UUID. Only use if you know what you are doing!
     */
    public EntityCreature(@NotNull EntityType entityType, @NotNull UUID uuid) {
        super(entityType, uuid);
        heal();
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
        this.navigator.setPathFinder(new HydrazinePathFinder(navigator.getPathingEntity(), instance.getInstanceSpace()));

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

    @Override
    public Collection<EntityAIGroup> getAIGroups() {
        return aiGroups;
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
