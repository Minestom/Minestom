package net.minestom.server.coordinate;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a collection of aligned coordinates in a 3D space.
 * <p>
 * If switched over, consider a fallback to the iterator as more implementations may be added in the future.
 */
@ApiStatus.Experimental
public sealed interface Area extends Iterable<Vec> {
    static @NotNull Area.Cuboid cuboid(@NotNull Point min, @NotNull Point max) {
        return new AreaImpl.Cuboid(min, max);
    }

    static @NotNull Area.Sphere sphere(@NotNull Point center, int radius) {
        return new AreaImpl.Sphere(center, radius);
    }

    sealed interface Cuboid extends Area permits AreaImpl.Cuboid {
        @NotNull Point min();

        @NotNull Point max();
    }

    sealed interface Sphere extends Area permits AreaImpl.Sphere {
        @NotNull Point center();

        int radius();
    }
}
