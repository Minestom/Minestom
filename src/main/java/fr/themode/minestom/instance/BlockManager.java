package fr.themode.minestom.instance;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BlockManager {

    private Map<Integer, CustomBlock> blocksInternalId = new HashMap<>();
    private Map<String, CustomBlock> blocksId = new HashMap<>();

    public void registerBlock(Supplier<CustomBlock> blocks) {
        CustomBlock customBlock = blocks.get();
        String identifier = customBlock.getIdentifier();
        int id = customBlock.getId();
        this.blocksInternalId.put(id, customBlock);
        this.blocksId.put(identifier, customBlock);
    }

    public CustomBlock getBlock(String identifier) {
        return blocksId.get(identifier);
    }

    public CustomBlock getBlock(int id) {
        return blocksInternalId.get(id);
    }

}
