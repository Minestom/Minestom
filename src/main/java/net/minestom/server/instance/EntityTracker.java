package net.minestom.server.instance;

import net.minestom.server.Viewable;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Defines how {@link Entity entities} are tracked within an {@link Instance instance}.
 * <p>
 * Implementations are expected to be thread-safe.
 */
public sealed interface EntityTracker permits EntityTrackerImpl {
    static @NotNull EntityTracker newTracker() {
        return new EntityTrackerImpl();
    }

    /**
     * Register an entity to be tracked.
     */
    <T extends Entity> void register(@NotNull Entity entity, @NotNull Point point,
                                     @NotNull Target<T> target, @Nullable Update<T> update);

    /**
     * Unregister an entity tracking.
     */
    <T extends Entity> void unregister(@NotNull Entity entity, @NotNull Target<T> target, @Nullable Update<T> update);

    /**
     * Gets an entity based on its id (from {@link Entity#getEntityId()}).
     *
     * @param id the entity id
     * @return the entity having the specified id, null if not found
     */
    @Nullable Entity getEntityById(int id);

    /**
     * Gets an entity based on its UUID (from {@link Entity#getUuid()}).
     *
     * @param uuid the entity UUID
     * @return the entity having the specified uuid, null if not found
     */
    @Nullable Entity getEntityByUuid(UUID uuid);

    /**
     * Called every time an entity move, you may want to verify if the new
     * position is in a different chunk.
     */
    <T extends Entity> void move(@NotNull Entity entity, @NotNull Point newPoint,
                                 @NotNull Target<T> target, @Nullable Update<T> update);

    @UnmodifiableView <T extends Entity> Collection<T> chunkEntities(int chunkX, int chunkZ, @NotNull Target<T> target);

    @UnmodifiableView
    default <T extends Entity> @NotNull Collection<T> chunkEntities(@NotNull Point point, @NotNull Target<T> target) {
        return chunkEntities(point.chunkX(), point.chunkZ(), target);
    }

    /**
     * Gets the entities within a chunk range.
     */
    <T extends Entity> void nearbyEntitiesByChunkRange(@NotNull Point point, int chunkRange,
                                                       @NotNull Target<T> target, @NotNull Consumer<T> query);

    /**
     * Gets the entities within a range.
     */
    <T extends Entity> void nearbyEntities(@NotNull Point point, double range,
                                           @NotNull Target<T> target, @NotNull Consumer<T> query);

    /**
     * Gets all the entities tracked by this class.
     */
    @UnmodifiableView
    @NotNull <T extends Entity> Set<@NotNull T> entities(@NotNull Target<T> target);

    @UnmodifiableView
    default @NotNull Set<@NotNull Entity> entities() {
        return entities(Target.ENTITIES);
    }

    @NotNull Viewable viewable(@NotNull List<@NotNull SharedInstance> sharedInstances, int chunkX, int chunkZ);

    default @NotNull Viewable viewable(int chunkX, int chunkZ) {
        return viewable(List.of(), chunkX, chunkZ);
    }

    /**
     * Represents the type of entity you want to retrieve.
     *
     * @param <E> the entity type
     */
    @ApiStatus.NonExtendable
    interface Target<E extends Entity> {
        Target<Entity> ENTITIES = create(Entity.class);
        Target<Player> PLAYERS = create(Player.class);
        Target<ItemEntity> ITEMS = create(ItemEntity.class);
        Target<ExperienceOrb> EXPERIENCE_ORBS = create(ExperienceOrb.class);

        List<EntityTracker.Target<? extends Entity>> TARGETS = List.of(EntityTracker.Target.ENTITIES, EntityTracker.Target.PLAYERS, EntityTracker.Target.ITEMS, EntityTracker.Target.EXPERIENCE_ORBS);

        Class<E> type();

        int ordinal();

        private static <T extends Entity> EntityTracker.Target<T> create(Class<T> type) {
            final int ordinal = EntityTrackerImpl.TARGET_COUNTER.getAndIncrement();
            return new Target<>() {
                @Override
                public Class<T> type() {
                    return type;
                }

                @Override
                public int ordinal() {
                    return ordinal;
                }
            };
        }
    }

    /**
     * Callback to know the newly visible entities and those to remove.
     */
    interface Update<E extends Entity> {
        void add(@NotNull E entity);

        void remove(@NotNull E entity);

        default void referenceUpdate(@NotNull Point point, @Nullable EntityTracker tracker) {
            // Empty
        }
    }
}
