package net.minestom.server.instance.block.rule.vanilla;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.block.BlockUtils;
import org.jetbrains.annotations.NotNull;

public class RedstonePlacementRule extends BlockPlacementRule {

    public RedstonePlacementRule() {
        super(Block.REDSTONE_WIRE);
    }

    @Override
    public boolean canPlace(@NotNull Instance instance, @NotNull BlockPosition blockPosition) {
        BlockUtils block = new BlockUtils(instance, blockPosition);
        return block.below().getBlock().isSolid();
    }

    @Override
    public short blockRefresh(@NotNull Instance instance, @NotNull BlockPosition blockPosition, short currentId) {
        BlockUtils block = new BlockUtils(instance, blockPosition);

        String pEast = "none";
        String pNorth = "none";
        String power = "0";
        String pSouth = "none";
        String pWest = "none";

        int connected = 0;

        BlockUtils north = block.north();
        BlockUtils south = block.south();
        BlockUtils east = block.east();
        BlockUtils west = block.west();

        // TODO: Block should have method isRedstone, as redstone connects to more than itself.

        if (north.equals(Block.REDSTONE_WIRE) || north.below().equals(Block.REDSTONE_WIRE)) {
            connected++;
            pNorth = "side";
        }
        if (south.equals(Block.REDSTONE_WIRE) || south.below().equals(Block.REDSTONE_WIRE)) {
            connected++;
            pSouth = "side";
        }
        if (east.equals(Block.REDSTONE_WIRE) || east.below().equals(Block.REDSTONE_WIRE)) {
            connected++;
            pEast = "side";
        }
        if (west.equals(Block.REDSTONE_WIRE) || west.below().equals(Block.REDSTONE_WIRE)) {
            connected++;
            pWest = "side";
        }
        if (north.above().equals(Block.REDSTONE_WIRE)) {
            connected++;
            pNorth = "up";
        }
        if (east.above().equals(Block.REDSTONE_WIRE)) {
            connected++;
            pEast = "up";
        }
        if (south.above().equals(Block.REDSTONE_WIRE)) {
            connected++;
            pSouth = "up";
        }
        if (west.above().equals(Block.REDSTONE_WIRE)) {
            connected++;
            pWest = "up";
        }
        if (connected == 0) {
            pNorth = "side";
            pEast = "side";
            pSouth = "side";
            pWest = "side";
        } else if (connected == 1) {
            if (!pNorth.equals("none")) pSouth = "side";
            if (!pSouth.equals("none")) pNorth = "side";
            if (!pEast.equals("none")) pWest = "side";
            if (!pWest.equals("none")) pEast = "side";
        }

        // TODO power

        final String[] properties = {
                "east=" + pEast,
                "north=" + pNorth,
                "power=" + power,
                "south=" + pSouth,
                "west=" + pWest};

        return Block.REDSTONE_WIRE.withProperties(properties);
    }

    @Override
    public short blockPlace(@NotNull Instance instance, @NotNull Block block, @NotNull BlockFace blockFace, @NotNull Player pl) {
        return getBlockId();
    }

    private boolean isRedstone(Instance instance, int x, int y, int z) {
        final short blockStateId = instance.getBlockStateId(x, y, z);
        return Block.fromStateId(blockStateId) == Block.REDSTONE_WIRE;
    }

    private boolean isAir(Instance instance, int x, int y, int z) {
        final short blockStateId = instance.getBlockStateId(x, y, z);
        return Block.fromStateId(blockStateId) == Block.AIR;
    }

}
