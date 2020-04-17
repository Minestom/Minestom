package fr.themode.minestom.instance.block.rule.vanilla;

import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.block.Block;
import fr.themode.minestom.instance.block.rule.BlockPlacementRule;
import fr.themode.minestom.utils.BlockPosition;

public class RedstonePlacementRule extends BlockPlacementRule {

    public RedstonePlacementRule() {
        super(Block.REDSTONE_WIRE);
    }

    @Override
    public boolean canPlace(Instance instance, BlockPosition blockPosition) {
        // TODO check solid block
        return true;
    }

    @Override
    public short blockRefresh(Instance instance, BlockPosition blockPosition) {
        int x = blockPosition.getX();
        int y = blockPosition.getY();
        int z = blockPosition.getZ();

        if (isAir(instance, x, y - 1, z)) {
            return Block.AIR.getBlockId();
        }


        String east = "none";
        String north = "none";
        String power = "0";
        String south = "none";
        String west = "none";

        if (isRedstone(instance, x + 1, y + 1, z)) {
            east = "up";
        } else if (isRedstone(instance, x + 1, y, z)) {
            east = "side";
        } else if (isRedstone(instance, x + 1, y - 1, z)) {
            east = "side";
        }

        if (isRedstone(instance, x - 1, y + 1, z)) {
            west = "up";
        } else if (isRedstone(instance, x - 1, y, z)) {
            west = "side";
        } else if (isRedstone(instance, x - 1, y - 1, z)) {
            west = "side";
        }

        if (isRedstone(instance, x, y + 1, z + 1)) {
            south = "up";
        } else if (isRedstone(instance, x, y, z + 1)) {
            south = "side";
        } else if (isRedstone(instance, x, y - 1, z + 1)) {
            south = "side";
        }

        if (isRedstone(instance, x, y + 1, z - 1)) {
            north = "up";
        } else if (isRedstone(instance, x, y, z - 1)) {
            north = "side";
        } else if (isRedstone(instance, x, y - 1, z - 1)) {
            north = "side";
        }

        // TODO power


        return Block.REDSTONE_WIRE.withProperties(east, north, power, south, west);
    }

    private boolean isRedstone(Instance instance, int x, int y, int z) {
        short blockId = instance.getBlockId(x, y, z);
        return Block.getBlockFromId(blockId) == Block.REDSTONE_WIRE;
    }

    private boolean isAir(Instance instance, int x, int y, int z) {
        short blockId = instance.getBlockId(x, y, z);
        return Block.getBlockFromId(blockId) == Block.AIR;
    }

}
