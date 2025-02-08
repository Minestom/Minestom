package net.minestom.server.entity;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

record EntitySelectorImpl<E>(EntitySelector.Target target,
                             EntitySelector.Sort sort,
                             int limit,
                             List<BiPredicate<Point, E>> conditions) implements EntitySelector<E> {
    public EntitySelectorImpl {
        conditions = List.copyOf(conditions);
    }

    @Override
    public boolean test(Point point, E entity) {
        for (var condition : conditions) {
            if (!condition.test(point, entity)) return false;
        }
        return true;
    }

    record PropertyImpl<E, T>(String name, Function<E, T> function) implements EntitySelector.Property<E, T> {
    }

    static final class BuilderImpl<E> implements Builder<E> {
        private Target target = Target.ALL_ENTITIES;
        private Sort sort = Sort.ARBITRARY;
        private int limit = -1;
        private final List<BiPredicate<Point, E>> conditions = new ArrayList<>();

        @Override
        public void target(@NotNull Target target) {
            this.target = target;
        }

        @Override
        public <T> void predicate(@NotNull Property<? super E, T> property, @NotNull BiPredicate<Point, T> predicate) {
            this.conditions.add((point, entity) -> predicate.test(point, property.function().apply(entity)));
        }

        @Override
        public void type(@NotNull EntityType @NotNull ... types) {
            predicate(Property.class.cast(EntitySelectors.TYPE), (point, type) -> new HashSet<>(List.of(types)).contains(type));
        }

        @Override
        public void range(double radius) {
            this.<Pos>predicate(Property.class.cast(EntitySelectors.POS),
                    (origin, coord) -> origin.distance(coord) <= radius);
        }

        @Override
        public void chunk(int chunkX, int chunkZ) {
            this.<Pos>predicate(Property.class.cast(EntitySelectors.POS),
                    (origin, coord) -> coord.chunkX() == chunkX && coord.chunkZ() == chunkZ);
        }

        @Override
        public void chunkRange(int radius) {
            this.<Pos>predicate(Property.class.cast(EntitySelectors.POS), (origin, coord) -> {
                final int originChunkX = origin.chunkX();
                final int originChunkZ = origin.chunkZ();
                final int coordChunkX = coord.chunkX();
                final int coordChunkZ = coord.chunkZ();
                final int deltaX = Math.abs(originChunkX - coordChunkX);
                final int deltaZ = Math.abs(originChunkZ - coordChunkZ);
                return deltaX <= radius && deltaZ <= radius;
            });
        }

        @Override
        public void sort(@NotNull Sort sort) {
            this.sort = sort;
        }

        @Override
        public void limit(int limit) {
            this.limit = limit;
        }

        EntitySelectorImpl<E> build() {
            return new EntitySelectorImpl<>(target, sort, limit, conditions);
        }
    }
}
