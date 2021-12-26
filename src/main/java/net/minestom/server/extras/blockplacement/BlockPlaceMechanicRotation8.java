package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.NamespaceID;

import java.util.HashSet;
import java.util.Set;

/**
 * 8 directional rotation for signs & banners
 */
class BlockPlaceMechanicRotation8 {

    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        float playerYaw = event.getPlayer().getPosition().yaw();
        int rotation = (int)Math.floor((double)((180.0F + playerYaw) * 16.0F / 360.0F) + 0.5D) & 15; // From vanilla (SignBlock#getPlacementState)

        event.setBlock(event.getBlock().withProperty("rotation", ""+rotation));
    }

}
