package net.minestom.server.instance.block.rule.vanilla;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

public class FencePlacementRule extends BlockPlacementRule {

    Block block;

    public FencePlacementRule(Block block) {
        super(block);
        this.block = block;
    }

    @Override
    public short blockUpdate(@NotNull Instance instance, @NotNull BlockPosition blockPosition, short currentId) {
        final int x = blockPosition.getX();
        final int y = blockPosition.getY();
        final int z = blockPosition.getZ();

        String east = "false";
        String north = "false";
        String south = "false";
        String waterlogged = "false";
        String west = "false";

        if (isBlock(instance, x + 1, y, z)) {
            east = "true";
        }

        if (isBlock(instance, x - 1, y, z)) {
            west = "true";
        }

        if (isBlock(instance, x, y, z + 1)) {
            south = "true";
        }

        if (isBlock(instance, x, y, z - 1)) {
            north = "true";
        }

        return block.withProperties("east=" + east, "north=" + north, "south=" + south,
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
