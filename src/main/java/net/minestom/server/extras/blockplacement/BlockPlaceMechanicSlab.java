package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

final class BlockPlaceMechanicSlab {
    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        BlockPlaceMechanicHalf.onPlace(block, event, "type");
        block = event.getBlock();

        final BlockFace face = event.getBlockFace();
        final Point position = event.getBlockPosition();
        final Instance instance = event.getInstance();

        if (face == BlockFace.TOP || face == BlockFace.BOTTOM) {
            final Point placedOn = position.add(0, face == BlockFace.BOTTOM ? 1 : -1, 0);

            Block placedOnState = instance.getBlock(placedOn);

            if (placedOnState.compare(block)) {
                final String oldType = placedOnState.getProperty("type");
                final String newType = block.getProperty("type");

                if (!oldType.equalsIgnoreCase("double") &&
                        oldType.equalsIgnoreCase(newType)) {
                    placedOnState = placedOnState.withProperty("type", "double");
                    instance.setBlock(placedOn, placedOnState);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        Block blockAt = instance.getBlock(position);
        if (blockAt.compare(block)) {
            event.setBlock(block.withProperty("type", "double"));
        }
    }
}
