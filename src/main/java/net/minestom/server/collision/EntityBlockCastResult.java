package net.minestom.server.collision;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 *  The result of a {@link Ray#cast(Block.Getter, Collection)}
 */
public interface EntityBlockCastResult extends BlockCastResult, EntityCastResult {
    /**
     * Generates a list of entities that intersected the ray
     * before the collisionThreshold-th block was intersected.
     *
     * @param collisionThreshold the amount of blocks the ray could pass through
     *                           before entities are no longer included
     * @return                   the ordered list of entities before the
     *                           block collisionThreshold is reached
     */
    @NotNull List<EntityRayCollision> findEntitiesBeforeBlockCollision(int collisionThreshold);

    /**
     * Generate a list of entities that intersected the ray
     * before the ray intersected a block.
     *
     * @see EntityBlockCastResult#findEntitiesBeforeBlockCollision(int)
     */
    @NotNull List<EntityRayCollision> findEntitiesBeforeBlockCollision();

    /**
     * Generates a list of blocks that intersected the ray
     * before the collisionThreshold-th entity was intersected.
     *
     * @param collisionThreshold the amount of entities the ray could pass through
     *                           before blocks are no longer included
     * @return                   the ordered list of blocks before the
     *                           entity collisionThreshold is reached
     */
    @NotNull List<BlockRayCollision> findBlocksBeforeEntityCollision(int collisionThreshold);

    /**
     * Generate a list of blocks that intersected the ray
     * before the ray intersected an entity.
     *
     * @see EntityBlockCastResult#findBlocksBeforeEntityCollision(int)
     */
    @NotNull List<BlockRayCollision> findBlocksBeforeEntityCollision();
}
