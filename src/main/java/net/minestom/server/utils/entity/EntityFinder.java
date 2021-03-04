package net.minestom.server.utils.entity;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.math.IntRange;
import net.minestom.server.utils.validate.Check;
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
    private String constantName;
    private UUID constantUuid;
    private final ToggleableMap<String> names = new ToggleableMap<>();
    private final ToggleableMap<UUID> uuids = new ToggleableMap<>();


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

    public EntityFinder setConstantName(@NotNull String constantName) {
        this.constantName = constantName;
        return this;
    }

    public EntityFinder setConstantUuid(@NotNull UUID constantUuid) {
        this.constantUuid = constantUuid;
        return this;
    }

    public EntityFinder setName(@NotNull String name, @NotNull ToggleableType toggleableType) {
        this.names.put(name, toggleableType.getValue());
        return this;
    }

    public EntityFinder setUuid(@NotNull UUID uuid, @NotNull ToggleableType toggleableType) {
        this.uuids.put(uuid, toggleableType.getValue());
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
     * @param instance the instance to search from,
     *                 null if the query can be executed using global data (all online players)
     * @param self     the source of the query, null if not any
     * @return all entities validating the conditions, can be empty
     */
    @NotNull
    public List<Entity> find(@Nullable Instance instance, @Nullable Entity self) {
        if (targetSelector == TargetSelector.MINESTOM_USERNAME) {
            Check.notNull(constantName, "The player name should not be null when searching for it");
            final Player player = MinecraftServer.getConnectionManager().getPlayer(constantName);
            return player != null ? Collections.singletonList(player) : Collections.emptyList();
        } else if (targetSelector == TargetSelector.MINESTOM_UUID) {
            Check.notNull(constantUuid, "The UUID should not be null when searching for it");
            final Entity entity = Entity.getEntity(constantUuid);
            return entity != null ? Collections.singletonList(entity) : Collections.emptyList();
        }

        List<Entity> result = findTarget(instance, targetSelector, startPosition, self);

        // Fast exit if there is nothing to process
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
            result = result.stream().filter(entity -> {
                if (!(entity instanceof Player))
                    return false;
                return filterToggleableMap(entity, ((Player) entity).getUsername(), names);
            }).collect(Collectors.toList());
        }

        // UUID
        if (!uuids.isEmpty()) {
            result = result.stream().filter(entity -> {
                return filterToggleableMap(entity, entity.getUuid(), uuids);
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

    @NotNull
    public List<Entity> find(@NotNull CommandSender sender) {
        if (sender.isPlayer()) {
            Player player = sender.asPlayer();
            return find(player.getInstance(), player);
        } else {
            return find(null, null);
        }
    }

    /**
     * Shortcut of {@link #find(Instance, Entity)} to retrieve the first
     * player element in the list.
     *
     * @return the first player returned by {@link #find(Instance, Entity)}
     * @see #find(Instance, Entity)
     */
    @Nullable
    public Player findFirstPlayer(@Nullable Instance instance, @Nullable Entity self) {
        final List<Entity> entities = find(instance, self);
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                return (Player) entity;
            }
        }
        return null;
    }

    @Nullable
    public Player findFirstPlayer(@NotNull CommandSender sender) {
        if (sender.isPlayer()) {
            final Player player = sender.asPlayer();
            return findFirstPlayer(player.getInstance(), player);
        } else {
            return findFirstPlayer(null, null);
        }
    }

    @Nullable
    public Entity findFirstEntity(@Nullable Instance instance, @Nullable Entity self) {
        final List<Entity> entities = find(instance, self);
        for (Entity entity : entities) {
            return entity;
        }
        return null;
    }

    @Nullable
    public Entity findFirstEntity(@NotNull CommandSender sender) {
        if (sender.isPlayer()) {
            final Player player = sender.asPlayer();
            return findFirstEntity(player.getInstance(), player);
        } else {
            return findFirstEntity(null, null);
        }
    }

    public enum TargetSelector {
        NEAREST_PLAYER, RANDOM_PLAYER, ALL_PLAYERS, ALL_ENTITIES, SELF, MINESTOM_USERNAME, MINESTOM_UUID
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
    }

    @NotNull
    private static List<Entity> findTarget(@Nullable Instance instance, @NotNull TargetSelector targetSelector,
                                           @NotNull Position startPosition, @Nullable Entity self) {

        if (targetSelector == TargetSelector.NEAREST_PLAYER) {
            Entity entity = null;
            double closestDistance = Double.MAX_VALUE;

            Collection<Player> instancePlayers = instance != null ?
                    instance.getPlayers() : MinecraftServer.getConnectionManager().getOnlinePlayers();
            for (Player player : instancePlayers) {
                final double distance = player.getPosition().getDistance(startPosition);
                if (distance < closestDistance) {
                    entity = player;
                    closestDistance = distance;
                }
            }
            return Collections.singletonList(entity);
        } else if (targetSelector == TargetSelector.RANDOM_PLAYER) {
            Collection<Player> instancePlayers = instance != null ?
                    instance.getPlayers() : MinecraftServer.getConnectionManager().getOnlinePlayers();
            final int index = ThreadLocalRandom.current().nextInt(instancePlayers.size());
            final Player player = instancePlayers.stream().skip(index).findFirst().orElseThrow();
            return Collections.singletonList(player);
        } else if (targetSelector == TargetSelector.ALL_PLAYERS) {
            return new ArrayList<>(instance != null ?
                    instance.getPlayers() : MinecraftServer.getConnectionManager().getOnlinePlayers());
        } else if (targetSelector == TargetSelector.ALL_ENTITIES) {
            return new ArrayList<>(instance.getEntities());
        } else if (targetSelector == TargetSelector.SELF) {
            return Collections.singletonList(self);
        }
        throw new IllegalStateException("Weird thing happened: " + targetSelector);
    }

    private static <T> boolean filterToggleableMap(@NotNull Entity entity, @NotNull T value, @NotNull ToggleableMap<T> map) {
        if (!(entity instanceof Player))
            return false;

        for (Object2BooleanMap.Entry<T> entry : map.object2BooleanEntrySet()) {
            final T key = entry.getKey();
            final boolean include = entry.getBooleanValue();

            final boolean equals = Objects.equals(value, key);
            if (include && !equals || !include && equals) {
                return false;
            }
        }

        return true;
    }
}
