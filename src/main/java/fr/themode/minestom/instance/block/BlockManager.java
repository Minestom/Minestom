package fr.themode.minestom.instance.block;

import fr.themode.minestom.instance.block.rule.BlockPlacementRule;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;

public class BlockManager {

    private Short2ObjectMap<CustomBlock> blocksInternalId = new Short2ObjectOpenHashMap<>();
    private Map<String, CustomBlock> blocksId = new HashMap<>();

    private Map<Block, BlockPlacementRule> placementRules = new HashMap<>();

    public void registerCustomBlock(CustomBlock customBlock) {
        String identifier = customBlock.getIdentifier();
        short id = customBlock.getId();
        this.blocksInternalId.put(id, customBlock);
        this.blocksId.put(identifier, customBlock);
    }

    public void registerBlockPlacementRule(BlockPlacementRule blockPlacementRule) {
        this.placementRules.put(blockPlacementRule.getBlock(), blockPlacementRule);
    }

    public BlockPlacementRule getBlockPlacementRule(short blockId) {
        Block block = Block.getBlockFromId(blockId);
        return this.placementRules.get(block);
    }

    public CustomBlock getBlock(String identifier) {
        return blocksId.get(identifier);
    }

    public CustomBlock getBlock(short id) {
        return blocksInternalId.get(id);
    }

}
