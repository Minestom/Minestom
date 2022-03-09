package net.minestom.server.collision;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@ApiStatus.Internal
@ApiStatus.Experimental
public final class CollisionUtils {

    /**
     * Moves an entity with physics applied (ie checking against blocks)
     * <p>
     * Works by getting all the full blocks that an entity could interact with.
     * All bounding boxes inside the full blocks are checked for collisions with the entity.
     *
     * @param entity the entity to move
     * @return the result of physics simulation
     */
    public static PhysicsResult handlePhysics(@NotNull Entity entity, @NotNull Vec entityVelocity,
                                              @Nullable PhysicsResult lastPhysicsResult) {
        return BlockCollision.handlePhysics(entity, entityVelocity, lastPhysicsResult);
    }

    public static PhysicsResult handlePhysics(@NotNull Entity entity, @NotNull Vec entityVelocity) {
        return handlePhysics(entity, entityVelocity, null);
    }

    /**
     * Check if a moving entity will collide with a block. Updates finalResult
     *
     * @param blockX         block x position
     * @param blockY         block y position
     * @param blockZ         block z position
     * @param entityVelocity entity movement vector
     * @param entityPosition entity position
     * @param boundingBox    entity bounding box
     * @param instance       entity instance
     * @param originChunk    entity chunk
     * @param finalResult    place to store final result of collision
     * @return true if entity finds collision, other false
     */
    public static boolean checkBoundingBox(int blockX, int blockY, int blockZ, Vec entityVelocity, Pos entityPosition, BoundingBox boundingBox, Instance instance, Chunk originChunk, SweepResult finalResult) {
        final Chunk c = ChunkUtils.retrieve(instance, originChunk, blockX, blockZ);
        // Don't step if chunk isn't loaded yet
        Block checkBlock = !ChunkUtils.isLoaded(c) ? Block.STONE : c.getBlock(blockX, blockY, blockZ, Block.Getter.Condition.TYPE);

        boolean hitBlock = false;

        Pos blockPos = new Pos(blockX, blockY, blockZ);

        if (checkBlock.isSolid()) {
            hitBlock = boundingBox.intersectBlockSwept(entityPosition, entityVelocity, checkBlock, blockPos, finalResult);
        }

        return hitBlock;
    }

    /**
     * Applies world border collision.
     *
     * @param instance        the instance where the world border is
     * @param currentPosition the current position
     * @param newPosition     the future target position
     * @return the position with the world border collision applied (can be {@code newPosition} if not changed)
     */
    public static @NotNull Pos applyWorldBorder(@NotNull Instance instance,
                                                @NotNull Pos currentPosition, @NotNull Pos newPosition) {
        final WorldBorder worldBorder = instance.getWorldBorder();
        final WorldBorder.CollisionAxis collisionAxis = worldBorder.getCollisionAxis(newPosition);
        return switch (collisionAxis) {
            case NONE ->
                // Apply velocity + gravity
                    newPosition;
            case BOTH ->
                // Apply Y velocity/gravity
                    new Pos(currentPosition.x(), newPosition.y(), currentPosition.z());
            case X ->
                // Apply Y/Z velocity/gravity
                    new Pos(currentPosition.x(), newPosition.y(), newPosition.z());
            case Z ->
                // Apply X/Y velocity/gravity
                    new Pos(newPosition.x(), newPosition.y(), currentPosition.z());
        };
    }

    public static Shape parseBlockShape(String str, Supplier<Material> block) {
        return ShapeImpl.parseBlockFromRegistry(str, block);
    }
}
