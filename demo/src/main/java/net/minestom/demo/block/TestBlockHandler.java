package net.minestom.demo.block;

import net.minestom.server.event.block.BlockChangeEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

public class TestBlockHandler implements BlockHandler {
    public static final BlockHandler INSTANCE = new TestBlockHandler();

    @Override
    public void onPlace(@NotNull BlockChangeEvent event) {
        System.out.println(event);
    }

    @Override
    public void onDestroy(@NotNull BlockChangeEvent event) {
        System.out.println(event);
    }

    @Override
    public @NotNull Block getBlock() {
        return Block.STONE;
    }
}
