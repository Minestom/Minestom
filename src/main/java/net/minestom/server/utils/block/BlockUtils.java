package net.minestom.server.utils.block;

import net.minestom.server.world.World;
import net.minestom.server.block.Block;
import net.minestom.server.utils.BlockPosition;

public class BlockUtils {

    private final World world;
    private final BlockPosition position;

    public BlockUtils(World world, BlockPosition position) {
        this.world = world;
        this.position = position;
    }

    public BlockUtils getRelativeTo(int x, int y, int z) {
        BlockPosition position = this.position.clone().add(x, y, z);
        return new BlockUtils(world, position);
    }

    public BlockUtils above() {
        return getRelativeTo(0, 1, 0);
    }

    public BlockUtils below() {
        return getRelativeTo(0, -1, 0);
    }

    public BlockUtils north() {
        return getRelativeTo(0, 0, -1);
    }

    public BlockUtils east() {
        return getRelativeTo(1, 0, 0);
    }

    public BlockUtils south() {
        return getRelativeTo(0, 0, 1);
    }

    public BlockUtils west() {
        return getRelativeTo(-1, 0, 0);
    }

    public Block getBlock() {
        return world.getBlock(position);
    }

    public boolean equals(Block block) {
        return getBlock() == block;
    }
}
