package net.minestom.server.extras.blockplacement;

import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;

class BlockInteractMechanicOpen {

    static void onInteract(Block block, PlayerBlockInteractEvent event) {
        block = event.getBlock();

        boolean open = "true".equals(block.getProperty("open"));

        event.setBlock(block.withProperty("open", open ? "false" : "true"));
        event.setBlockingItemUse(true);
    }

}
