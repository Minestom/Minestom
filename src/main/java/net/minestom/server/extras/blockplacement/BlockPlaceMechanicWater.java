package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Direction;

class BlockPlaceMechanicWater {

    static void onInteract(ItemStack itemStack, PlayerUseItemOnBlockEvent event) {
        Block block = event.getInstance().getBlock(event.getPosition());

        if (!block.isAir()) {
            if (block.compare(Block.CAULDRON)) {
                event.getInstance().setBlock(event.getPosition(), Block.WATER_CAULDRON.withProperty("level", "3"));
                return;
            } else {
                String waterlogged = block.getProperty("waterlogged");
                if ("false".equals(waterlogged)) {
                    event.getInstance().setBlock(event.getPosition(), block.withProperty("waterlogged", "true"));
                    return;
                }
            }
        }

        Direction dir = event.getBlockFace();
        Point pos = event.getPosition().add(dir.normalX(), dir.normalY(), dir.normalZ());
        block = event.getInstance().getBlock(pos);
        if (block.isAir()) {
            event.getInstance().setBlock(pos, Block.WATER);
        }
    }

}
