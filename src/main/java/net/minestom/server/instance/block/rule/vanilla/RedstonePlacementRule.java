package net.minestom.server.instance.block.rule.vanilla;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.block.BlockUtils;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RedstonePlacementRule extends BlockPlacementRule {

    public RedstonePlacementRule() {
        super(Block.REDSTONE_WIRE);
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull Instance instance, @NotNull Point blockPosition, @NotNull Block block) {
        BlockUtils blockUtils = new BlockUtils(instance, blockPosition);

        String east = "none";
        String north = "none";
        String power = "0";
        String south = "none";
        String west = "none";

        // TODO Block should have method isRedstone, as redstone connects to more than itself.

        final BlockUtils blockNorth = blockUtils.north();
        final BlockUtils blockSouth = blockUtils.south();
        final BlockUtils blockEast = blockUtils.east();
        final BlockUtils blockWest = blockUtils.west();
        int connected = 0;

        if (blockNorth.equals(Block.REDSTONE_WIRE) || blockNorth.below().equals(Block.REDSTONE_WIRE)) {
            connected++;
            north = "side";
        }
        if (blockSouth.equals(Block.REDSTONE_WIRE) || blockSouth.below().equals(Block.REDSTONE_WIRE)) {
            connected++;
            south = "side";
        }
        if (blockEast.equals(Block.REDSTONE_WIRE) || blockEast.below().equals(Block.REDSTONE_WIRE)) {
            connected++;
            east = "side";
        }
        if (blockWest.equals(Block.REDSTONE_WIRE) || blockWest.below().equals(Block.REDSTONE_WIRE)) {
            connected++;
            west = "side";
        }
        if (blockNorth.above().equals(Block.REDSTONE_WIRE)) {
            connected++;
            north = "up";
        }
        if (blockSouth.above().equals(Block.REDSTONE_WIRE)) {
            connected++;
            south = "up";
        }
        if (blockEast.above().equals(Block.REDSTONE_WIRE)) {
            connected++;
            east = "up";
        }
        if (blockWest.above().equals(Block.REDSTONE_WIRE)) {
            connected++;
            west = "up";
        }
        if (connected == 0) {
            north = "side";
            south = "side";
            east = "side";
            west = "side";
        } else if (connected == 1) {
            if (!north.equals("none")) {
                south = "side";
            }
            if (!south.equals("none")) {
                north = "side";
            }
            if (!east.equals("none")) {
                west = "side";
            }
            if (!west.equals("none")) {
                east = "side";
            }
        }

        // TODO power
        return Block.REDSTONE_WIRE.withProperties(Map.of(
                "east", east,
                "north", north,
                "south", south,
                "west", west,
                "power", power));
    }

    @Override
    public Block blockPlace(@NotNull Instance instance,
                            @NotNull Block block, @NotNull BlockFace blockFace, @NotNull Point blockPosition,
                            @NotNull Player pl) {
        final Block belowBlock = instance.getBlock(blockPosition.sub(0, 1, 0));
        return belowBlock.isSolid() ? block : null;
    }
}
