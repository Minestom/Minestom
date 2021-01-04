package net.minestom.server.instance.block;

import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BlockManager {

    // custom block id -> custom block
    private final CustomBlock[] customBlocksInternalId = new CustomBlock[Short.MAX_VALUE];
    // custom block identifier -> custom block
    private final Map<String, CustomBlock> customBlocksId = new HashMap<>();

    // block id -> block placement rule
    private final BlockPlacementRule[] placementRules = new BlockPlacementRule[Short.MAX_VALUE];

    /**
     * Registers a {@link CustomBlock}.
     *
     * @param customBlock the custom block to register
     * @throws IllegalArgumentException if <code>customBlock</code> block id is not greater than 0
     * @throws IllegalStateException    if the id of <code>customBlock</code> is already registered
     */
    public synchronized void registerCustomBlock(@NotNull CustomBlock customBlock) {
        final short id = customBlock.getCustomBlockId();
        Check.argCondition(id <= 0, "Custom block ID must be greater than 0, got: " + id);
        Check.stateCondition(customBlocksInternalId[id] != null, "a CustomBlock with the id " + id + " already exists");

        final String identifier = customBlock.getIdentifier();
        this.customBlocksInternalId[id] = customBlock;
        this.customBlocksId.put(identifier, customBlock);
    }

    /**
     * Registers a {@link BlockPlacementRule}.
     *
     * @param blockPlacementRule the block placement rule to register
     * @throws IllegalArgumentException if <code>blockPlacementRule</code> block id is negative
     */
    public synchronized void registerBlockPlacementRule(@NotNull BlockPlacementRule blockPlacementRule) {
        final short id = blockPlacementRule.getBlockId();
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
        return this.placementRules[block.getBlockId()];
    }

    /**
     * Gets the {@link BlockPlacementRule} of the specific block.
     *
     * @param blockStateId the block id to check
     * @return the block placement rule associated with the id, null if not any
     */
    @Nullable
    public BlockPlacementRule getBlockPlacementRule(short blockStateId) {
        final Block block = Block.fromStateId(blockStateId); // Convert block alternative
        return getBlockPlacementRule(block);
    }

    /**
     * Gets the {@link CustomBlock} with the specific identifier {@link CustomBlock#getIdentifier()}.
     *
     * @param identifier the custom block identifier
     * @return the {@link CustomBlock} associated with the identifier, null if not any
     */
    @Nullable
    public CustomBlock getCustomBlock(@NotNull String identifier) {
        return customBlocksId.get(identifier);
    }

    /**
     * Gets the {@link CustomBlock} with the specific custom block id {@link CustomBlock#getCustomBlockId()}.
     *
     * @param id the custom block id
     * @return the {@link CustomBlock} associated with the id, null if not any
     */
    @Nullable
    public CustomBlock getCustomBlock(short id) {
        return customBlocksInternalId[id];
    }

    /**
     * Gets all the registered custom blocks.
     *
     * @return a {@link Collection} containing the registered custom blocks
     */
    @NotNull
    public Collection<CustomBlock> getCustomBlocks() {
        return customBlocksId.values();
    }

}
