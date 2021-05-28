package net.minestom.server.instance.block;

import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockManager {

    // block id -> block placement rule
    private final BlockPlacementRule[] placementRules = new BlockPlacementRule[Short.MAX_VALUE];

    /**
     * Registers a {@link BlockPlacementRule}.
     *
     * @param blockPlacementRule the block placement rule to register
     * @throws IllegalArgumentException if <code>blockPlacementRule</code> block id is negative
     */
    public synchronized void registerBlockPlacementRule(@NotNull BlockPlacementRule blockPlacementRule) {
        final int id = blockPlacementRule.getBlock().getId();
        Check.argCondition(id < 0, "Block ID must be >= 0, got: " + id);

        this.placementRules[id] = blockPlacementRule;
    }

    /**
     * Gets the {@link BlockPlacementRule} of the specific block.
     *
     * @param block the block to check
     * @return the block placement rule associated with the block, null if not any
     */
    @Nullable
    public BlockPlacementRule getBlockPlacementRule(@NotNull Block block) {
        return this.placementRules[block.getId()];
    }
}
