package net.minestom.server.utils.entity;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.math.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

// TODO

/**
 * Represents a query which can be call to find one or multiple entities.
 * It is based on the target selectors used in commands.
 */
public class EntityFinder {

    private TargetSelector targetSelector;

    private EntitySort entitySort = EntitySort.ARBITRARY;

    // Position
    private Position startPosition = new Position();
    private Float dx, dy, dz;
    private IntRange distance;

    // By traits
    private Integer limit;
    private final ToggleableMap<EntityType> entityTypes = new ToggleableMap<>();
    private final ToggleableMap<String> names = new ToggleableMap<>();

    // Players specific
    private final ToggleableMap<GameMode> gameModes = new ToggleableMap<>();
    private IntRange level;

    public EntityFinder setTargetSelector(@NotNull TargetSelector targetSelector) {
        this.targetSelector = targetSelector;
        return this;
    }

    public EntityFinder setEntitySort(@NotNull EntitySort entitySort) {
        this.entitySort = entitySort;
        return this;
    }

    public EntityFinder setStartPosition(@NotNull Position startPosition) {
        this.startPosition = startPosition;
        return this;
    }

    public EntityFinder setDistance(@NotNull IntRange distance) {
        this.distance = distance;
        return this;
    }

    public EntityFinder setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public EntityFinder setLevel(@NotNull IntRange level) {
        this.level = level;
        return this;
    }

    public EntityFinder setEntity(@NotNull EntityType entityType, @NotNull ToggleableType toggleableType) {
        this.entityTypes.put(entityType, toggleableType.getValue());
        return this;
    }

    public EntityFinder setName(@NotNull String name, @NotNull ToggleableType toggleableType) {
        this.names.put(name, toggleableType.getValue());
        return this;
    }

    public EntityFinder setGameMode(@NotNull GameMode gameMode, @NotNull ToggleableType toggleableType) {
        this.gameModes.put(gameMode, toggleableType.getValue());
        return this;
    }

    public EntityFinder setDifference(float dx, float dy, float dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        return this;
    }

    /**
     * Find a list of entities (could be empty) based on the conditions
     *
     * @return all entities validating the conditions, can be empty
     */
    @NotNull
    public List<Entity> find(@NotNull Instance instance, @Nullable Entity self) {
        List<Entity> result = findTarget(instance, targetSelector, startPosition, self);

        // Fast exist if there is nothing to process
        if (result.isEmpty())
            return result;

        // Distance argument
        if (distance != null) {
            final int minDistance = distance.getMinimum();
            final int maxDistance = distance.getMaximum();
            result = result.stream().filter(entity -> {
                final int distance = (int) entity.getDistance(self);
                return MathUtils.isBetween(distance, minDistance, maxDistance);
            }).collect(Collectors.toList());
        }

        // Diff X/Y/Z
        if (dx != null || dy != null || dz != null) {
            result = result.stream().filter(entity -> {
                final Position entityPosition = entity.getPosition();
                if (dx != null && !MathUtils.isBetweenUnordered(
                        entityPosition.getX(),
                        startPosition.getX(), dx))
                    return false;

                if (dy != null && !MathUtils.isBetweenUnordered(
                        entityPosition.getY(),
                        startPosition.getY(), dy))
                    return false;

                if (dz != null && !MathUtils.isBetweenUnordered(
                        entityPosition.getZ(),
                        startPosition.getZ(), dz))
                    return false;

                return true;
            }).collect(Collectors.toList());
        }

        // Entity type
        if (!entityTypes.isEmpty()) {
            result = result.stream().filter(entity ->
                    filterToggleableMap(entity, entity.getEntityType(), entityTypes))
                    .collect(Collectors.toList());
        }

        // GameMode
        if (!gameModes.isEmpty()) {
            final GameMode requirement = gameModes.requirement;
            result = result.stream().filter(entity -> {
                if (!(entity instanceof Player))
                    return false;
                return filterToggleableMap(entity, ((Player) entity).getGameMode(), gameModes);
            }).collect(Collectors.toList());
        }

        // Level
        if (level != null) {
            final int minLevel = level.getMinimum();
            final int maxLevel = level.getMaximum();
            result = result.stream().filter(entity -> {
                if (!(entity instanceof Player))
                    return false;

                final int level = ((Player) entity).getLevel();
                return MathUtils.isBetween(level, minLevel, maxLevel);
            }).collect(Collectors.toList());
        }

        // Name
        if (!names.isEmpty()) {
            // TODO entity name
            result = result.stream().filter(entity -> {
                if (!(entity instanceof Player))
                    return false;
                return filterToggleableMap(entity, ((Player) entity).getUsername(), names);
            }).collect(Collectors.toList());
        }


        // Sort & limit
        if (entitySort != EntitySort.ARBITRARY || limit != null) {
            result = result.stream()
                    .sorted((ent1, ent2) -> {
                        switch (entitySort) {
                            case ARBITRARY:
                            case RANDOM:
                                // RANDOM is handled below
                                return 1;
                            case FURTHEST:
                                return startPosition.getDistance(ent1.getPosition()) >
                                        startPosition.getDistance(ent2.getPosition()) ?
                                        1 : 0;
                            case NEAREST:
                                return startPosition.getDistance(ent1.getPosition()) <
                                        startPosition.getDistance(ent2.getPosition()) ?
                                        1 : 0;
                        }
                        return 1;
                    })
                    .limit(limit != null ? limit : Integer.MAX_VALUE)
                    .collect(Collectors.toList());

            if (entitySort == EntitySort.RANDOM) {
                Collections.shuffle(result);
            }
        }


        return result;
    }

    public enum TargetSelector {
        NEAREST_PLAYER, RANDOM_PLAYER, ALL_PLAYERS, ALL_ENTITIES, SELF
    }

    public enum EntitySort {
        ARBITRARY, FURTHEST, NEAREST, RANDOM
    }

    public enum ToggleableType {
        INCLUDE(true), EXCLUDE(false);

        private final boolean value;

        ToggleableType(boolean value) {
            this.value = value;
        }

        public boolean getValue() {
            return value;
        }
    }

    private static class ToggleableMap<T> extends Object2BooleanOpenHashMap<T> {

        @Nullable
        private T requirement;

    }

    @NotNull
    private static List<Entity> findTarget(@NotNull Instance instance, @NotNull TargetSelector targetSelector,
                                           @NotNull Position startPosition, @Nullable Entity self) {

        if (targetSelector == TargetSelector.NEAREST_PLAYER) {
            Entity entity = null;
            float closestDistance = Float.MAX_VALUE;

            Set<Player> instancePlayers = instance.getPlayers();
            for (Player player : instancePlayers) {
                final float distance = player.getPosition().getDistance(startPosition);
                if (distance < closestDistance) {
                    entity = player;
                    closestDistance = distance;
                }
            }
            return Arrays.asList(entity);
        } else if (targetSelector == TargetSelector.RANDOM_PLAYER) {
            Set<Player> instancePlayers = instance.getPlayers();
            final int index = ThreadLocalRandom.current().nextInt(instancePlayers.size());
            final Player player = instancePlayers.stream().skip(index).findFirst().orElseThrow();
            return Arrays.asList(player);
        } else if (targetSelector == TargetSelector.ALL_PLAYERS) {
            return new ArrayList<>(instance.getPlayers());
        } else if (targetSelector == TargetSelector.ALL_ENTITIES) {
            return new ArrayList<>(instance.getEntities());
        } else if (targetSelector == TargetSelector.SELF) {
            return Arrays.asList(self);
        }
        throw new IllegalStateException("Weird thing happened");
    }

    private static <T> boolean filterToggleableMap(@NotNull Entity entity, @NotNull T value, @NotNull ToggleableMap<T> map) {
        if (!(entity instanceof Player))
            return false;

        final T requirement = map.requirement;
        // true if the entity type has not been mentioned or if is accepted
        return (!map.containsKey(value) && requirement == null) ||
                Objects.equals(requirement, value) ||
                map.getBoolean(value);
    }
}
