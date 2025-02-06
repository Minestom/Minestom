package net.minestom.server.entity;

import java.util.List;
import java.util.function.BiPredicate;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

record EntityQueryImpl(EntityQuery.Target target,
                       EntityQuery.Sort sort,
                       int limit,
                       List<EntityQuery.Condition<Object>> conditions) implements EntityQuery {
    public EntityQueryImpl {
        conditions = List.copyOf(conditions);
    }

    record Index<T>() implements EntityQuery.Property<T> {
    }

    record ConditionImpl<T>(Property<T> property, BiPredicate<Point, T> predicate) implements Condition<T> {
        @Override
        public boolean test(@NotNull Point origin, @NotNull T value) {
            return predicate.test(origin, value);
        }
    }
}
