package net.minestom.server.utils.entity;

import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.math.IntRange;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

// TODO

/**
 * Represents a query which can be call to find one or multiple entities.
 * It is based on the target selectors used in commands.
 */
public class EntityFinder {

    private TargetSelector targetSelector;

    private EntitySort entitySort = EntitySort.ARBITRARY;

    // Position
    private Point startPosition;
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

    public EntityFinder setStartPosition(@NotNull Point startPosition) {
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
    public @NotNull List<@NotNull Entity> find(@Nullable Instance instance, @Nullable Entity self) {
        if (targetSelector == TargetSelector.MINESTOM_USERNAME) {
            Check.notNull(constantName, "The player name should not be null when searching for it");
            final Player player = MinecraftServer.getConnectionManager().getPlayer(constantName);
            return player != null ? Collections.singletonList(player) : Collections.emptyList();
        } else if (targetSelector == TargetSelector.MINESTOM_UUID) {
            Check.notNull(constantUuid, "The UUID should not be null when searching for it");
            final Entity entity = Entity.getEntity(constantUuid);
            return entity != null ? Collections.singletonList(entity) : Collections.emptyList();
        }

        final Point pos = startPosition != null ? startPosition : (self != null ? self.getPosition() : Vec.ZERO);

        List<Entity> result = findTarget(instance, targetSelector, pos, self);
        // Fast exit if there is nothing to process
        if (result.isEmpty())
            return result;

        // Distance argument
        if (distance != null) {
            final int minDistance = distance.getMinimum();
            final int maxDistance = distance.getMaximum();
            result = result.stream()
                    .filter(entity -> MathUtils.isBetween(entity.getDistance(pos), minDistance, maxDistance))
                    .toList();
        }

        // Diff X/Y/Z
        if (dx != null || dy != null || dz != null) {
            result = result.stream().filter(entity -> {
                final var entityPosition = entity.getPosition();
                if (dx != null && !MathUtils.isBetweenUnordered(
                        entityPosition.x(),
                        pos.x(), dx))
                    return false;

                if (dy != null && !MathUtils.isBetweenUnordered(
                        entityPosition.y(),
                        pos.y(), dy))
                    return false;

                if (dz != null && !MathUtils.isBetweenUnordered(
                        entityPosition.z(),
                        pos.z(), dz))
                    return false;

                return true;
            }).toList();
        }

        // Entity type
        if (!entityTypes.isEmpty()) {
            result = result.stream()
                    .filter(entity -> filterToggleableMap(entity.getEntityType(), entityTypes))
                    .toList();
        }

        // GameMode
        if (!gameModes.isEmpty()) {
            result = result.stream()
                    .filter(Player.class::isInstance)
                    .filter(entity -> filterToggleableMap(((Player) entity).getGameMode(), gameModes))
                    .toList();
        }

        // Level
        if (level != null) {
            final int minLevel = level.getMinimum();
            final int maxLevel = level.getMaximum();
            result = result.stream()
                    .filter(Player.class::isInstance)
                    .filter(entity -> MathUtils.isBetween(((Player) entity).getLevel(), minLevel, maxLevel))
                    .toList();
        }

        // Name
        if (!names.isEmpty()) {
            result = result.stream()
                    .filter(Player.class::isInstance)
                    .filter(entity -> filterToggleableMap(((Player) entity).getUsername(), names))
                    .toList();
        }

        // UUID
        if (!uuids.isEmpty()) {
            result = result.stream()
                    .filter(entity -> filterToggleableMap(entity.getUuid(), uuids))
                    .toList();
        }


        // Sort & limit
        if (entitySort != EntitySort.ARBITRARY || limit != null) {
            result = result.stream()
                    .sorted((ent1, ent2) -> switch (entitySort) {
                        case ARBITRARY, RANDOM ->
                                // RANDOM is handled below
                                1;
                        case FURTHEST -> pos.distance(ent1.getPosition()) >
                                pos.distance(ent2.getPosition()) ?
                                1 : 0;
                        case NEAREST -> pos.distance(ent1.getPosition()) <
                                pos.distance(ent2.getPosition()) ?
                                1 : 0;
                    })
                    .limit(limit != null ? limit : Integer.MAX_VALUE)
                    .toList();

            if (entitySort == EntitySort.RANDOM) {
                Collections.shuffle(result);
            }
        }

        return result;
    }

    public @NotNull List<@NotNull Entity> find(@NotNull CommandSender sender) {
        return sender instanceof Player player ?
                find(player.getInstance(), player) : find(null, null);
    }

    /**
     * Shortcut of {@link #find(Instance, Entity)} to retrieve the first
     * player element in the list.
     *
     * @return the first player returned by {@link #find(Instance, Entity)}
     * @see #find(Instance, Entity)
     */
    public @Nullable Player findFirstPlayer(@Nullable Instance instance, @Nullable Entity self) {
        final List<Entity> entities = find(instance, self);
        for (Entity entity : entities) {
            if (entity instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    public @Nullable Player findFirstPlayer(@NotNull CommandSender sender) {
        return sender instanceof Player player ?
                findFirstPlayer(player.getInstance(), player) :
                findFirstPlayer(null, null);
    }

    public @Nullable Entity findFirstEntity(@Nullable Instance instance, @Nullable Entity self) {
        final List<Entity> entities = find(instance, self);
        return entities.isEmpty() ? null : entities.get(0);
    }

    public @Nullable Entity findFirstEntity(@NotNull CommandSender sender) {
        return sender instanceof Player player ?
                findFirstEntity(player.getInstance(), player) : findFirstEntity(null, null);
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

    private static @NotNull List<@NotNull Entity> findTarget(@Nullable Instance instance,
                                                             @NotNull TargetSelector targetSelector,
                                                             @NotNull Point startPosition, @Nullable Entity self) {
        final var players = instance != null ?
                instance.getPlayers() : MinecraftServer.getConnectionManager().getOnlinePlayers();
        if (targetSelector == TargetSelector.NEAREST_PLAYER) {
            return players.stream()
                    .min(Comparator.comparingDouble(p -> p.getPosition().distance(startPosition)))
                    .<List<Entity>>map(Collections::singletonList).orElse(Collections.emptyList());
        } else if (targetSelector == TargetSelector.RANDOM_PLAYER) {
            final int index = ThreadLocalRandom.current().nextInt(players.size());
            final Player player = players.stream().skip(index).findFirst().orElseThrow();
            return Collections.singletonList(player);
        } else if (targetSelector == TargetSelector.ALL_PLAYERS) {
            return List.copyOf(players);
        } else if (targetSelector == TargetSelector.ALL_ENTITIES) {
            if (instance != null) {
                return List.copyOf(instance.getEntities());
            }
            // Get entities from every instance
            var instances = MinecraftServer.getInstanceManager().getInstances();
            List<Entity> entities = new ArrayList<>();
            for (Instance inst : instances) {
                entities.addAll(inst.getEntities());
            }
            return entities;
        } else if (targetSelector == TargetSelector.SELF) {
            return self != null ? Collections.singletonList(self) : Collections.emptyList();
        }
        throw new IllegalStateException("Weird thing happened: " + targetSelector);
    }

    private static <T> boolean filterToggleableMap(@NotNull T value, @NotNull ToggleableMap<T> map) {
        for (var entry : Object2BooleanMaps.fastIterable(map)) {
            if (entry.getBooleanValue() != Objects.equals(value, entry.getKey())) {
                return false;
            }
        }
        return true;
    }
}
