package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;

class BlockInteractMechanicLever {

    static void onInteract(Block block, PlayerBlockInteractEvent event) {
        if (event.getPlayer().isSneaking()) return;

        block = event.getBlock();

        boolean powered = "true".equals(block.getProperty("powered"));

        String value = powered ? "false" : "true";

        event.setBlock(block.withProperty("powered", value));
        event.setBlockingItemUse(true);
    }

}
