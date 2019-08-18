package fr.themode.minestom.instance;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BlockManager {

    private Map<String, CustomBlock> blocks = new HashMap<>();

    public void registerBlock(String id, Supplier<CustomBlock> blocks) {
        this.blocks.put(id, blocks.get());
    }

    public CustomBlock getBlock(String id) {
        return this.blocks.get(id);
    }

}
