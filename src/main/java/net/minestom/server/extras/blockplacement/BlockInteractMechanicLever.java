package net.minestom.server.extras.blockplacement;

import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;

final class BlockInteractMechanicLever {
    static void onInteract(Block block, PlayerBlockInteractEvent event) {
        if (event.getPlayer().isSneaking()) return;
        block = event.getBlock();

        final boolean powered = Boolean.parseBoolean(block.getProperty("powered"));
        final String value = powered ? "false" : "true";

        event.setBlock(block.withProperty("powered", value));
        event.setBlockingItemUse(true);
    }
}
