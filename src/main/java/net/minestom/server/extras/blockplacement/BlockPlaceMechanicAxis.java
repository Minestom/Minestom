package net.minestom.server.extras.blockplacement;

import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;

class BlockPlaceMechanicAxis {

    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        block = event.getBlock();

        switch(event.getBlockFace()) {
            case EAST: case WEST:
                event.setBlock(block.withProperty("axis", "x")); return;
            case NORTH: case SOUTH:
                event.setBlock(block.withProperty("axis", "z")); return;
            default:
                event.setBlock(block.withProperty("axis", "y"));
        }
    }

}
