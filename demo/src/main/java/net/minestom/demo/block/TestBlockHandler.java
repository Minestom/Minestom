package net.minestom.demo.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.block.BlockChangeEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public class TestBlockHandler implements BlockHandler {
    public static final BlockHandler INSTANCE = new TestBlockHandler();

    @Override
    public void onBlockChange(@NotNull BlockChangeEvent event) {
//        System.out.println("onBlockChange " + event.getBlock());
    }

    @Override
    public Block onNeighborUpdate(@NotNull Block neighbor, @NotNull Instance instance, @NotNull Point point, @NotNull BlockFace fromFace) {
//        System.out.println("onNeighborUpdate " + neighbor);
        return neighbor;
    }

    @Override
    public @NotNull Block getBlock() {
        return Block.STONE;
    }
}