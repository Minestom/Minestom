package net.minestom.demo.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockChange;
import net.minestom.server.instance.block.BlockHandler;

public class TestBlockHandler implements BlockHandler {
    public static final BlockHandler INSTANCE = new TestBlockHandler();

    @Override
    public Key getKey() {
        return Key.key("minestom", "test");
    }

    @Override
    public Block onPlace(BlockChange blockChange) {
        System.out.println("onPlace");
        return Block.DIAMOND_BLOCK;
    }

    @Override
    public Block onDestroy(BlockChange blockChange) {
        System.out.println("onDestroy");
        return Block.EMERALD_BLOCK;
    }
}
