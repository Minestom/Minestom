package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Represents an immutable ray which can be cast to find intersections
 * through blocks and entities.
 *
 * @param origin        the starting point of this ray
 * @param direction     the direction of this ray
 * @param distance      the distance this ray travels
 * @param configuration the additional configuration properties for this ray
 */
public record Ray(@NotNull Point origin, @NotNull Vec direction, double distance, @NotNull Configuration configuration) {
    /**
     * @throws IllegalStateException if distance is less than 0
     */
    public Ray {
        if (origin instanceof Pos) origin = Vec.fromPoint(origin);
        if (!direction.isNormalized()) {
            direction = direction.normalize();
        }
        Check.stateCondition(distance < 0, "Distance cannot be less than 0");
    }

    @Contract(pure = true)
    private Ray(@NotNull Point origin, @NotNull Vec direction, double distance, @NotNull UnaryOperator<Configuration.Builder> unaryOperator) {
        this(origin, direction, distance, unaryOperator.apply(Configuration.builder()).build());
    }

    @Contract(pure = true)
    public Ray(@NotNull Point origin, @NotNull Vec direction, double distance, @NotNull Consumer<Configuration.Builder> consumer) {
        this(origin, direction, distance, builder -> {
            consumer.accept(builder);
            return builder;
        });
    }

    @Contract(pure = true)
    public Ray(@NotNull Point origin, @NotNull Vec direction, double distance) {
        this(origin, direction, distance, Configuration.DEFAULT);
    }

    @Contract(pure = true)
    public @NotNull Ray withOrigin(@NotNull Point origin) {
        return new Ray(origin, direction, distance, configuration);
    }

    @Contract(pure = true)
    public @NotNull Ray withDirection(@NotNull Vec direction) {
        return new Ray(origin, direction, distance, configuration);
    }

    @Contract(pure = true)
    public @NotNull Ray withDistance(double distance) {
        return new Ray(origin, direction, distance, configuration);
    }

    @Contract(pure = true)
    public @NotNull Ray withConfiguration(@NotNull Configuration configuration) {
        return new Ray(origin, direction, distance, configuration);
    }

    @Contract(pure = true)
    public @NotNull Ray withConfiguration(@NotNull Consumer<Configuration.Builder> consumer) {
        Configuration.Builder builder = Configuration.builder();
        consumer.accept(builder);
        return new Ray(origin, direction, distance, builder.build());
    }

    /**
     * Cast this ray against the target entity and block bounding boxes.
     * <p>
     * Note: entity and block collisions are determined independently,
     * intersecting blocks will never stop proceeding entities from being collected;
     * the resulting {@link EntityBlockCastResult} gives you tools to filter
     * blocks/entities with this logic.
     *
     * @param blockGetter the {@link Block.Getter} supplying blocks to cast the ray against
     * @param entities    the entities to cast the ray against
     * @return            an {@link EntityBlockCastResult} containing the intersections of this ray
     */
    public @NotNull EntityBlockCastResult cast(@NotNull Block.Getter blockGetter, @NotNull Collection<Entity> entities) {
        return RaycastUtils.performEntityBlockCast(this, blockGetter, entities);
    }

    /**
     * Cast this ray against the target entity bounding boxes.
     *
     * @param entities the entities to cast the ray against
     * @return         an {@link EntityCastResult} containing the entities intersected
     *                 by this ray
     */
    public @NotNull EntityCastResult cast(@NotNull Collection<Entity> entities) {
        return RaycastUtils.performEntityCast(this, entities);
    }

    /**
     * Cast this ray against the target block bounding boxes.
     *
     * @param blockGetter the {@link Block.Getter} supplying blocks to cast the ray against
     * @return            the {@link BlockCastResult} containing the blocks intersected by this ray
     */
    public @NotNull BlockCastResult cast(@NotNull Block.Getter blockGetter) {
        return RaycastUtils.performBlockCast(this, blockGetter);
    }

    /**
     * Represent the collision configuration for a given ray
     *
     * @param blockFilter                the filter blocks must pass to be considered for collision
     *                                   note: air is implicitly filtered
     *                                   (defaults to allow any block)
     * @param blockCollisionLimit        stop checking for blocks collisions beyond this limit
     *                                   (defaults to {@link Integer#MAX_VALUE})
     *                                   note: does not stop entity collision checks beyond this limit
     *                                       - Useful for optimization
     * @param entityFilter               the filter entities must pass to be considered for collision
     *                                   (defaults to allow any entity)
     * @param entityBoundingBoxExpansion amount to expand the entity {@link BoundingBox} axis to
     *                                   increase leniency in the ray cast
     *                                   (defaults to no expansion)
     * @param computeSurfaceNormals      true if surface normals should be computed
     *                                   (defaults to false)
     */
    public record Configuration(@NotNull Predicate<Block> blockFilter, int blockCollisionLimit, @NotNull Predicate<Entity> entityFilter,
                                @NotNull Vec entityBoundingBoxExpansion, boolean computeSurfaceNormals) {
        public static final Configuration DEFAULT = Configuration.builder().build();

        /**
         * @throws IllegalStateException if bounding box expansion components are less than 0
         */
        public Configuration {
            Check.stateCondition(entityBoundingBoxExpansion.x() < 0, "Bounding box x expansion cannot be less than 0");
            Check.stateCondition(entityBoundingBoxExpansion.y() < 0, "Bounding box y expansion cannot be less than 0");
            Check.stateCondition(entityBoundingBoxExpansion.z() < 0, "Bounding box z expansion cannot be less than 0");
        }

        public static class Builder {
            private Predicate<Block> blockFilter = block -> true;
            private int blockCollisionLimit = Integer.MAX_VALUE;
            private Predicate<Entity> entityFilter = entity -> true;
            private Vec entityBoundingBoxExpansion = Vec.ZERO;
            private boolean computeNormals = true;

            private @NotNull Configuration build() {
                return new Configuration(blockFilter, blockCollisionLimit, entityFilter, entityBoundingBoxExpansion, computeNormals);
            }

            public @NotNull Builder blockFilter(@NotNull Predicate<Block> blockFilter) {
                this.blockFilter = blockFilter;
                return this;
            }

            public @NotNull Builder blockCollisionLimit(int blockCollisionLimit) {
                this.blockCollisionLimit = blockCollisionLimit;
                return this;
            }

            public @NotNull Builder entityFilter(@NotNull Predicate<Entity> entityFilter) {
                this.entityFilter = entityFilter;
                return this;
            }

            public @NotNull Builder entityBoundingBoxExpansion(double x, double y, double z) {
                this.entityBoundingBoxExpansion = new Vec(x, y, z);
                return this;
            }

            public @NotNull Builder computeSurfaceNormals(boolean computeNormals) {
                this.computeNormals = computeNormals;
                return this;
            }

            private Builder() { }
        }

        private static @NotNull Builder builder() {
            return new Builder();
        }
    }
}
