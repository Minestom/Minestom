package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerBlockUpdateNeighborEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class BlockPlaceMechanicTwistingVines {

    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        event.setBlock(update(event.getBlock(), event.getBlockPosition(), event.getInstance()));
    }

    static void onNeighbor(Block block, PlayerBlockUpdateNeighborEvent event) {
        event.setBlock(update(event.getBlock(), event.getBlockPosition(), event.getInstance()));
    }

    private static Block update(Block block, Point position, Instance instance) {
        Block aboveBlock = instance.getBlock(position.blockX(), position.blockY()+1, position.blockZ());

        if (aboveBlock.compare(Block.TWISTING_VINES) ||
                aboveBlock.compare(Block.TWISTING_VINES_PLANT)) {
            return Block.TWISTING_VINES_PLANT;
        }
        return block;
    }

}
