package net.minestom.server.instance.block.rule.vanilla;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
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
    public short blockUpdate(@NotNull Instance instance, @NotNull BlockPosition blockPosition, short currentId) {
        final int x = blockPosition.getX();
        final int y = blockPosition.getY();
        final int z = blockPosition.getZ();

        String east = "none";
        String north = "none";
        String south = "none";
        String up = "true";
        String waterlogged = "false";
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


        return block.withProperties("east=" + east, "north=" + north, "south=" + south, "up=" + up,
                "waterlogged=" + waterlogged, "west=" + west);
    }

    @Override
    public short blockPlace(@NotNull Instance instance,
                            @NotNull Block block, @NotNull BlockFace blockFace, @NotNull BlockPosition blockPosition,
                            @NotNull Player pl) {
        return getBlockId();
    }

    private boolean isBlock(Instance instance, int x, int y, int z) {
        final short blockStateId = instance.getBlockStateId(x, y, z);
        return Block.fromStateId(blockStateId).isSolid();
    }

}
