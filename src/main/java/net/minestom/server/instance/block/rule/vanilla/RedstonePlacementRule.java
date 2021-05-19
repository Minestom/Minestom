package net.minestom.server.instance.block.rule.vanilla;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockProperties;
import net.minestom.server.instance.block.Blocks;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.block.BlockUtils;
import org.jetbrains.annotations.NotNull;

public class RedstonePlacementRule extends BlockPlacementRule {

    public RedstonePlacementRule() {
        super(Blocks.REDSTONE_WIRE);
    }

    @Override
    public Block blockUpdate(@NotNull Instance instance, @NotNull BlockPosition blockPosition, Block block) {
        BlockUtils blockUtils = new BlockUtils(instance, blockPosition);

        String east = "none";
        String north = "none";
        int power = 0;
        String south = "none";
        String west = "none";

        // TODO Block should have method isRedstone, as redstone connects to more than itself.

        final BlockUtils blockNorth = blockUtils.north();
        final BlockUtils blockSouth = blockUtils.south();
        final BlockUtils blockEast = blockUtils.east();
        final BlockUtils blockWest = blockUtils.west();
        int connected = 0;

        if (blockNorth.equals(Blocks.REDSTONE_WIRE) || blockNorth.below().equals(Blocks.REDSTONE_WIRE)) {
            connected++;
            north = "side";
        }
        if (blockSouth.equals(Blocks.REDSTONE_WIRE) || blockSouth.below().equals(Blocks.REDSTONE_WIRE)) {
            connected++;
            south = "side";
        }
        if (blockEast.equals(Blocks.REDSTONE_WIRE) || blockEast.below().equals(Blocks.REDSTONE_WIRE)) {
            connected++;
            east = "side";
        }
        if (blockWest.equals(Blocks.REDSTONE_WIRE) || blockWest.below().equals(Blocks.REDSTONE_WIRE)) {
            connected++;
            west = "side";
        }
        if (blockNorth.above().equals(Blocks.REDSTONE_WIRE)) {
            connected++;
            north = "up";
        }
        if (blockSouth.above().equals(Blocks.REDSTONE_WIRE)) {
            connected++;
            south = "up";
        }
        if (blockEast.above().equals(Blocks.REDSTONE_WIRE)) {
            connected++;
            east = "up";
        }
        if (blockWest.above().equals(Blocks.REDSTONE_WIRE)) {
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

        return Blocks.REDSTONE_WIRE.withProperty(BlockProperties.REDSTONE_WIRE.EAST_REDSTONE, east)
                .withProperty(BlockProperties.REDSTONE_WIRE.NORTH_REDSTONE, north)
                .withProperty(BlockProperties.REDSTONE_WIRE.SOUTH_REDSTONE, south)
                .withProperty(BlockProperties.REDSTONE_WIRE.WEST_REDSTONE, west)
                .withProperty(BlockProperties.REDSTONE_WIRE.POWER, power);
    }

    @Override
    public Block blockPlace(@NotNull Instance instance,
                            @NotNull Block block, @NotNull BlockFace blockFace, @NotNull BlockPosition blockPosition,
                            @NotNull Player pl) {
        final short belowBlockId = instance.getBlockStateId(blockPosition.getX(), blockPosition.getY() - 1, blockPosition.getZ());
        if (!Block.REGISTRY.fromStateId(belowBlockId).getData().isSolid()) {
            return null;
        }
        return block;
    }
}
