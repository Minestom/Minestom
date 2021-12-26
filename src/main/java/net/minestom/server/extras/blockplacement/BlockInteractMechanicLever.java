package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;

class BlockInteractMechanicOpen {

    static void onInteract(Block block, PlayerBlockInteractEvent event) {
        if (event.getPlayer().isSneaking()) return;

        block = event.getBlock();

        boolean open = "true".equals(block.getProperty("open"));

        String value = open ? "false" : "true";

        event.setBlock(block.withProperty("open", value));
        event.setBlockingItemUse(true);

        // Doors need to set the open property on both blocks
        String half = block.getProperty("half");
        if (half != null) {
            int offset = 1;
            if (half.equals("upper")) {
                offset = -1;
            }
            Point pos = event.getBlockPosition().add(0, offset, 0);
            Block offsetBlock = event.getInstance().getBlock(pos);
            if (offsetBlock.compare(block)) {
                event.getInstance().setBlock(pos, offsetBlock.withProperty("open", value));
            }
        }
    }

}
