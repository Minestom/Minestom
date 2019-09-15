package fr.themode.minestom.instance.block;

import fr.themode.minestom.Main;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.InstanceManager;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BlockManager {

    private static InstanceManager instanceManager = Main.getInstanceManager();

    private Short2ObjectMap<CustomBlock> blocksInternalId = new Short2ObjectOpenHashMap<>();
    private Map<String, CustomBlock> blocksId = new HashMap<>();

    public void registerBlock(Supplier<CustomBlock> blocks) {
        CustomBlock customBlock = blocks.get();
        String identifier = customBlock.getIdentifier();
        short id = customBlock.getId();
        this.blocksInternalId.put(id, customBlock);
        this.blocksId.put(identifier, customBlock);
    }

    public void update() {
        long time = System.currentTimeMillis();
        // TODO another thread pool
        for (Instance instance : instanceManager.getInstances()) {
            // FIXME: only InstanceContainer?
            for (Chunk chunk : instance.getChunks()) {
                chunk.updateBlocks(time, instance);
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
