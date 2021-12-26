package net.minestom.server.extras.blockplacement;

import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

final class BlockPlaceMechanicGlowLichen {
    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        final BlockFace face = event.getBlockFace().getOppositeFace();

        final String faceName = switch (face) {
            case TOP -> "up";
            case BOTTOM -> "down";
            default -> face.name().toLowerCase();
        };

        Block oldBlock = event.getInstance().getBlock(event.getBlockPosition());
        if (oldBlock.compare(block)) {
            block = block.withProperties(oldBlock.properties());
        }

        block = block.withProperty(faceName, "true");

        event.setBlock(block);
    }
}
