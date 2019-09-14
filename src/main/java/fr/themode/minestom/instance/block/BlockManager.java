package fr.themode.minestom.instance.block;

import fr.themode.minestom.Main;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.InstanceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BlockManager {

    private static InstanceManager instanceManager = Main.getInstanceManager();

    private Map<Short, CustomBlock> blocksInternalId = new HashMap<>();
    private Map<String, CustomBlock> blocksId = new HashMap<>();

    public void registerBlock(Supplier<CustomBlock> blocks) {
        CustomBlock customBlock = blocks.get();
        String identifier = customBlock.getIdentifier();
        short id = customBlock.getId();
        this.blocksInternalId.put(id, customBlock);
        this.blocksId.put(identifier, customBlock);
    }

    public void update() {
        for (Instance instance : instanceManager.getInstances()) {
            // TODO only InstanceContainer?
            for (Chunk chunk : instance.getChunks()) {
                chunk.updateBlocks();
            }
        }
    }

    public CustomBlock getBlock(String identifier) {
        return blocksId.get(identifier);
    }

    public CustomBlock getBlock(short id) {
        return blocksInternalId.get(id);
    }

}
