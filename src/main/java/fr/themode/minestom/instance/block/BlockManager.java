package fr.themode.minestom.instance.block;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;

public class BlockManager {

    private Short2ObjectMap<CustomBlock> blocksInternalId = new Short2ObjectOpenHashMap<>();
    private Map<String, CustomBlock> blocksId = new HashMap<>();

    public void registerBlock(CustomBlock block) {
        CustomBlock customBlock = block;
        String identifier = customBlock.getIdentifier();
        short id = customBlock.getId();
        this.blocksInternalId.put(id, customBlock);
        this.blocksId.put(identifier, customBlock);
    }

    public CustomBlock getBlock(String identifier) {
        return blocksId.get(identifier);
    }

    public CustomBlock getBlock(short id) {
        return blocksInternalId.get(id);
    }

}
