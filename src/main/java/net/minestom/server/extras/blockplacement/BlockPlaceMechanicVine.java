package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

public class BlockPlaceMechanicVine {

    public static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        BlockFace face = event.getBlockFace().getOppositeFace();
        Vec playerDir = event.getPlayer().getPosition().direction();

        // Find direction if placed on bottom
        if(face == BlockFace.BOTTOM) {
            if(Math.abs(playerDir.x()) > Math.abs(playerDir.z())) {
                if(playerDir.x() > 0) {
                    face = BlockFace.EAST;
                } else {
                    face = BlockFace.WEST;
                }
            } else {
                if(playerDir.z() > 0) {
                    face = BlockFace.SOUTH;
                } else {
                    face = BlockFace.NORTH;
                }
            }
        }

        String faceName = face.name().toLowerCase();
        if(face == BlockFace.TOP) faceName = "up";

        // Combine with previous vine states
        Block oldBlock = event.getInstance().getBlock(event.getBlockPosition());
        if(oldBlock.compare(block)) {
            block = block.withProperties(oldBlock.properties());
        }

        block = block.withProperty(faceName, "true");

        event.setBlock(block);
    }

}
