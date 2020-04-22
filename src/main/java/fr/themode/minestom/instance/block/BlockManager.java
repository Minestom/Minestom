package fr.themode.minestom.instance.block;

import fr.themode.minestom.instance.block.rule.BlockPlacementRule;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;

public class BlockManager {

    private Short2ObjectMap<CustomBlock> blocksInternalId = new Short2ObjectOpenHashMap<>();
    private Map<String, CustomBlock> blocksId = new HashMap<>();

    private Short2ObjectOpenHashMap<BlockPlacementRule> placementRules = new Short2ObjectOpenHashMap<>();

    public void registerCustomBlock(CustomBlock customBlock) {
        String identifier = customBlock.getIdentifier();
        short id = customBlock.getId();
        this.blocksInternalId.put(id, customBlock);
        this.blocksId.put(identifier, customBlock);
    }

    public void registerBlockPlacementRule(BlockPlacementRule blockPlacementRule) {
        this.placementRules.put(blockPlacementRule.getBlockId(), blockPlacementRule);
    }

    public BlockPlacementRule getBlockPlacementRule(short blockId) {
        Block block = Block.getFromId(blockId); // Convert block alternative
        blockId = block.getBlockId();
        return this.placementRules.get(blockId);
    }

    public BlockPlacementRule getBlockPlacementRule(Block block) {
        return getBlockPlacementRule(block.getBlockId());
    }

    public CustomBlock getBlock(String identifier) {
        return blocksId.get(identifier);
    }

    public CustomBlock getBlock(short id) {
        return blocksInternalId.get(id);
    }

}
