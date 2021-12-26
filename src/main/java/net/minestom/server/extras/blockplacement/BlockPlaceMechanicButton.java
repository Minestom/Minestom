package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

/**
 * Block mechanic for levers
 */
public class BlockPlaceMechanicButton {

    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        BlockFace face = event.getBlockFace();

        if(face == BlockFace.BOTTOM) {
            block = block.withProperty("face", "ceiling");
        } else if(face == BlockFace.TOP) {
            block = block.withProperty("face", "floor");
        } else {
            block = block.withProperty("face", "wall");
            block = block.withProperty("facing", face.name().toLowerCase());
            event.setBlock(block);
            return;
        }

        // When placing on the top/bottom of a block, set facing
        Vec playerDir = event.getPlayer().getPosition().direction();
        double absX = Math.abs(playerDir.x());
        double absZ = Math.abs(playerDir.z());
        if(absX > absZ) {
            if(playerDir.x() > 0) {
                block = block.withProperty("facing", "east");
            } else {
                block = block.withProperty("facing", "west");
            }
        } else {
            if(playerDir.z() > 0) {
                block = block.withProperty("facing", "south");
            } else {
                block = block.withProperty("facing", "north");
            }
        }

        event.setBlock(block);
    }
}
