package net.minestom.server.entity;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityQueryImpl.Index;
import net.minestom.server.utils.Range;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Descriptive how entities should be queried.
 * Offer potential indexing/spatial partitioning advantages over lazy looping.
 */
public sealed interface EntityQuery permits EntityQueryImpl {
    Property<Boolean> PLAYER = new Index<>();
    Property<Integer> ID = new Index<>();
    Property<UUID> UUID = new Index<>();
    Property<String> NAME = new Index<>();
    Property<Point> COORD = new Index<>();
    Property<Double> POS_X = new Index<>();
    Property<Double> POS_Y = new Index<>();
    Property<Double> POS_Z = new Index<>();
    Property<Float> YAW = new Index<>();
    Property<Float> PITCH = new Index<>();
    Property<Integer> CHUNK_X = new Index<>();
    Property<Integer> CHUNK_Z = new Index<>();
    Property<Double> DISTANCE = new Index<>();
    Property<EntityType> TYPE = new Index<>();
    Property<GameMode> GAME_MODE = new Index<>();
    Property<Integer> EXPERIENCE = new Index<>();

    @NotNull Target target();

    @NotNull Sort sort();

    int limit();

    @NotNull List<Condition<Object>> conditions();

    enum Target {
        NEAREST_PLAYER, RANDOM_PLAYER,
        ALL_PLAYERS, ALL_ENTITIES,
        SELF, NEAREST_ENTITY
    }

    enum Sort {
        ARBITRARY, FURTHEST, NEAREST, RANDOM
    }

    @SuppressWarnings("unused")
    sealed interface Property<T> permits EntityQueryImpl.Index {
    }

    sealed interface Condition<T> extends BiPredicate<Point, T> permits EntityQueryImpl.ConditionImpl {
        @NotNull Property<T> property();

        @Override
        boolean test(@NotNull Point origin, @NotNull T value);

        static <T> Condition<T> equalsCondition(Property<T> property, T value) {
            return __condition(property, (point, t) -> t.equals(value));
        }

        static <T> Condition<T> notEqualsCondition(Property<T> property, T value) {
            return __condition(property, (origin, t) -> !value.equals(t));
        }

        static <T extends Number> Condition<T> rangeCondition(Property<T> property, Range<T> range) {
            return __condition(property, (origin, t) -> switch (range) {
                case Range.Byte aByte -> aByte.inRange((Byte) t);
                case Range.Double aDouble -> aDouble.inRange((Double) t);
                case Range.Float aFloat -> aFloat.inRange((Float) t);
                case Range.Int anInt -> anInt.inRange((Integer) t);
                case Range.Long aLong -> aLong.inRange((Long) t);
                case Range.Short aShort -> aShort.inRange((Short) t);
            });
        }

        static Condition<Point> chunkRangeCondition(int radius) {
            return __condition(COORD, (origin, coord) -> {
                final int originChunkX = origin.chunkX();
                final int originChunkZ = origin.chunkZ();
                final int coordChunkX = coord.chunkX();
                final int coordChunkZ = coord.chunkZ();
                final int deltaX = Math.abs(originChunkX - coordChunkX);
                final int deltaZ = Math.abs(originChunkZ - coordChunkZ);
                return deltaX <= radius && deltaZ <= radius;
            });
        }

        static <T extends Number> Condition<T> higherCondition(Property<T> property, T value) {
            return __condition(property, (origin, t) -> t.doubleValue() > value.doubleValue());
        }

        static <T extends Number> Condition<T> higherEqualsCondition(Property<T> property, T value) {
            return __condition(property, (origin, t) -> t.doubleValue() >= value.doubleValue());
        }

        static <T extends Number> Condition<T> lowerCondition(Property<T> property, T value) {
            return __condition(property, (origin, t) -> t.doubleValue() < value.doubleValue());
        }

        static <T extends Number> Condition<T> lowerEqualsCondition(Property<T> property, T value) {
            return __condition(property, (origin, t) -> t.doubleValue() <= value.doubleValue());
        }

        /**
         * Allows any predicate which MUST be pure.
         * No guarantee as to when called, and may not take advantage of future optimizations.
         * <p>
         * Use at your own risk.
         */
        @ApiStatus.Internal
        static <T> Condition<T> __condition(Property<T> property, BiPredicate<Point, T> predicate) {
            return new EntityQueryImpl.ConditionImpl<>(property, predicate);
        }
    }

    static @NotNull EntityQuery entityQuery(EntityQuery.Target target,
                                            EntityQuery.Sort sort,
                                            int limit,
                                            List<EntityQuery.Condition<?>> conditions) {
        //noinspection rawtypes,unchecked
        return new EntityQueryImpl(target, sort, limit, (List) conditions);
    }

    static @NotNull EntityQuery entityQuery(EntityQuery.Target target,
                                            EntityQuery.Sort sort,
                                            int limit,
                                            EntityQuery.Condition<?>... conditions) {
        return entityQuery(target, sort, limit, List.of(conditions));
    }

    static @NotNull EntityQuery entityQuery(List<EntityQuery.Condition<?>> conditions) {
        return entityQuery(Target.ALL_ENTITIES, Sort.ARBITRARY, -1, conditions);
    }

    static @NotNull EntityQuery entityQuery(EntityQuery.Condition<?>... conditions) {
        return entityQuery(Target.ALL_ENTITIES, Sort.ARBITRARY, -1, List.of(conditions));
    }

    interface Finder {
        @NotNull Stream<@NotNull Entity> queryStream(@NotNull EntityQuery query, @NotNull Point origin);

        default @NotNull Stream<@NotNull Entity> queryStream(@NotNull EntityQuery query) {
            return queryStream(query, Vec.ZERO);
        }

        default void queryConsume(@NotNull EntityQuery query, @NotNull Point origin, Consumer<Entity> consumer) {
            final Stream<@NotNull Entity> stream = queryStream(query, origin);
            stream.forEach(consumer);
        }

        default void queryConsume(@NotNull EntityQuery query, Consumer<Entity> consumer) {
            queryConsume(query, Vec.ZERO, consumer);
        }

        default @Nullable Entity queryFirst(@NotNull EntityQuery query, @NotNull Point origin) {
            return queryStream(query, origin).findFirst().orElse(null);
        }

        default @Nullable Entity queryFirst(@NotNull EntityQuery query) {
            return queryFirst(query, Vec.ZERO);
        }
    }
}
