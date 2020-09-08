package net.minestom.server.instance.block;

import net.minestom.server.instance.block.rule.BlockPlacementRule;

import java.util.HashMap;
import java.util.Map;

public class BlockManager {

    // custom block id -> custom block
    private CustomBlock[] customBlocksInternalId = new CustomBlock[Short.MAX_VALUE];
    // custom block identifier -> custom block
    private Map<String, CustomBlock> customBlocksId = new HashMap<>();

    // block id -> block placement rule
    private BlockPlacementRule[] placementRules = new BlockPlacementRule[Short.MAX_VALUE];

    /**
     * Register a custom block
     *
     * @param customBlock the custom block to register
     * @throws IllegalArgumentException if {@param customBlock} block id is negative
     */
    public void registerCustomBlock(CustomBlock customBlock) {
        final short id = customBlock.getCustomBlockId();
        if (id < 0) throw new IllegalArgumentException("Custom block ID must be > 0, got: " + id);
        final String identifier = customBlock.getIdentifier();
        this.customBlocksInternalId[id] = customBlock;
        this.customBlocksId.put(identifier, customBlock);
    }

    /**
     * Register a block placement rule
     *
     * @param blockPlacementRule the block placement rule to register
     * @throws IllegalArgumentException if {@param blockPlacementRule} block id is negative
     */
    public void registerBlockPlacementRule(BlockPlacementRule blockPlacementRule) {
        final short id = blockPlacementRule.getBlockId();
        if (id < 0) throw new IllegalArgumentException("Block ID must be > 0, got: " + id);
        this.placementRules[id] = blockPlacementRule;
    }

    /**
     * Get the block placement rule of the specific block
     *
     * @param blockStateId the block id to check
     * @return the block placement rule associated with the id, null if not any
     */
    public BlockPlacementRule getBlockPlacementRule(short blockStateId) {
        final Block block = Block.fromStateId(blockStateId); // Convert block alternative
        final short blockId = block.getBlockId();
        return this.placementRules[blockId];
    }

    /**
     * Get the block placement rule of the specific block
     *
     * @param block the block to check
     * @return the block placement rule associated with the block, null if not any
     */
    public BlockPlacementRule getBlockPlacementRule(Block block) {
        return getBlockPlacementRule(block.getBlockId());
    }

    /**
     * Get the CustomBlock with the specific identifier {@link CustomBlock#getIdentifier()}
     *
     * @param identifier the custom block identifier
     * @return the {@link CustomBlock} associated with the identifier, null if not any
     */
    public CustomBlock getCustomBlock(String identifier) {
        return customBlocksId.get(identifier);
    }

    /**
     * Get the CustomBlock with the specific custom block id {@link CustomBlock#getCustomBlockId()}
     *
     * @param id the custom block id
     * @return the {@link CustomBlock} associated with the id, null if not any
     */
    public CustomBlock getCustomBlock(short id) {
        return customBlocksInternalId[id];
    }

}
