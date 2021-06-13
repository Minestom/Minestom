package net.minestom.server.block.rule.vanilla;

import net.minestom.server.block.Block;
import net.minestom.server.block.BlockFace;
import net.minestom.server.block.BlockProperties;
import net.minestom.server.block.rule.BlockPlacementRule;
import net.minestom.server.entity.Player;
import net.minestom.server.world.World;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

public class WallPlacementRule extends BlockPlacementRule {

    public WallPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull World world, @NotNull BlockPosition blockPosition, @NotNull Block block) {
        final int x = blockPosition.getX();
        final int y = blockPosition.getY();
        final int z = blockPosition.getZ();

        String east = "none";
        String north = "none";
        String south = "none";
        boolean up = true;
        boolean waterlogged = false;
        String west = "none";

        if (isBlock(world, x + 1, y, z)) {
            east = "low";
        }

        if (isBlock(world, x - 1, y, z)) {
            west = "low";
        }

        if (isBlock(world, x, y, z + 1)) {
            south = "low";
        }

        if (isBlock(world, x, y, z - 1)) {
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
    public Block blockPlace(@NotNull World world,
                            @NotNull Block block, @NotNull BlockFace blockFace, @NotNull BlockPosition blockPosition,
                            @NotNull Player pl) {
        return block;
    }

    private boolean isBlock(World world, int x, int y, int z) {
        return world.getBlock(x, y, z).isSolid();
    }
}
