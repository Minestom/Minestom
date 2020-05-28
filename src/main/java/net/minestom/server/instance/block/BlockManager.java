package net.minestom.server.instance.block;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

import java.util.HashMap;
import java.util.Map;

public class BlockManager {

    private Short2ObjectMap<CustomBlock> customBlocksInternalId = new Short2ObjectOpenHashMap<>();
    private Map<String, CustomBlock> customBlocksId = new HashMap<>();

    private Short2ObjectOpenHashMap<BlockPlacementRule> placementRules = new Short2ObjectOpenHashMap<>();

    /**
     * @param customBlock the custom block to register
     */
    public void registerCustomBlock(CustomBlock customBlock) {
        String identifier = customBlock.getIdentifier();
        short id = customBlock.getCustomBlockId();
        this.customBlocksInternalId.put(id, customBlock);
        this.customBlocksId.put(identifier, customBlock);
    }

    /**
     * @param blockPlacementRule the block placement rule to register
     */
    public void registerBlockPlacementRule(BlockPlacementRule blockPlacementRule) {
        this.placementRules.put(blockPlacementRule.getBlockId(), blockPlacementRule);
    }

    /**
     * @param blockId the block id to check
     * @return the block placement rule associated with the id, null if not any
     */
    public BlockPlacementRule getBlockPlacementRule(short blockId) {
        Block block = Block.fromId(blockId); // Convert block alternative
        blockId = block.getBlockId();
        return this.placementRules.get(blockId);
    }

    /**
     * @param block the block to check
     * @return the block placement rule associated with the block, null if not any
     */
    public BlockPlacementRule getBlockPlacementRule(Block block) {
        return getBlockPlacementRule(block.getBlockId());
    }

    /**
     * @param identifier the custom block identifier
     * @return the {@link CustomBlock} associated with the identifier, null if not any
     */
    public CustomBlock getCustomBlock(String identifier) {
        return customBlocksId.get(identifier);
    }

    /**
     * @param id the custom block id
     * @return the {@link CustomBlock} associated with the id, null if not any
     */
    public CustomBlock getCustomBlock(short id) {
        return customBlocksInternalId.get(id);
    }

}
