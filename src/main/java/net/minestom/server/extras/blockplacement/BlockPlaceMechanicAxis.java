package net.minestom.server.extras.blockplacement;

import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;

final class BlockPlaceMechanicAxis {
    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        block = event.getBlock();
        final String axis = switch (event.getBlockFace()) {
            case EAST, WEST -> "x";
            case NORTH, SOUTH -> "z";
            default -> "y";
        };
        event.setBlock(block.withProperty("axis", axis));
    }
}
