package net.minestom.server.instance.block.rule.vanilla;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockProperties;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

public class WallPlacementRule extends BlockPlacementRule {

    Block block;

    public WallPlacementRule(Block block) {
        super(block);
        this.block = block;
    }

    @Override
    public Block blockUpdate(@NotNull Instance instance, @NotNull BlockPosition blockPosition, Block block) {
        final int x = blockPosition.getX();
        final int y = blockPosition.getY();
        final int z = blockPosition.getZ();

        String east = "none";
        String north = "none";
        String south = "none";
        boolean up = true;
        boolean waterlogged = false;
        String west = "none";

        if (isBlock(instance, x + 1, y, z)) {
            east = "low";
        }

        if (isBlock(instance, x - 1, y, z)) {
            west = "low";
        }

        if (isBlock(instance, x, y, z + 1)) {
            south = "low";
        }

        if (isBlock(instance, x, y, z - 1)) {
            north = "low";
        }
        return block
                .withProperty(BlockProperties.NORTH_WALL, north)
                .withProperty(BlockProperties.EAST_WALL, east)
                .withProperty(BlockProperties.SOUTH_WALL, south)
                .withProperty(BlockProperties.WEST_WALL, west)
                .withProperty(BlockProperties.UP, up)
                .withProperty(BlockProperties.WATERLOGGED, waterlogged);
    }

    @Override
    public Block blockPlace(@NotNull Instance instance,
                            @NotNull Block block, @NotNull BlockFace blockFace, @NotNull BlockPosition blockPosition,
                            @NotNull Player pl) {
        return block;
    }

    private boolean isBlock(Instance instance, int x, int y, int z) {
        final short blockStateId = instance.getBlockStateId(x, y, z);
        return Block.REGISTRY.fromStateId(blockStateId).getData().isSolid();
    }

}
