package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerBlockUpdateNeighborEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class BlockPlaceMechanicWeepingVines {

    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        event.setBlock(update(event.getBlock(), event.getBlockPosition(), event.getInstance()));
    }

    static void onNeighbor(Block block, PlayerBlockUpdateNeighborEvent event) {
        event.setBlock(update(event.getBlock(), event.getBlockPosition(), event.getInstance()));
    }

    private static Block update(Block block, Point position, Instance instance) {
        Block belowBlock = instance.getBlock(position.blockX(), position.blockY()-1, position.blockZ());

        if (belowBlock.compare(Block.WEEPING_VINES) ||
                belowBlock.compare(Block.WEEPING_VINES_PLANT)) {
            return Block.WEEPING_VINES_PLANT;
        }
        return block;
    }

}
