package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerBlockUpdateNeighborEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;

class BlockPlaceMechanicBell {

    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        block = event.getBlock();
        Direction dir = event.getBlockFace().toDirection();
        event.setBlock(switch (dir) {
            case UP -> block.withProperty("attachment", "floor");
            case DOWN -> block.withProperty("attachment", "ceiling");
            case NORTH, SOUTH, WEST, EAST -> {
                Point pos = event.getBlockPosition();
                Block oppositeBlock = event.getInstance().getBlock(pos.blockX()+dir.normalX(),
                        pos.blockY()+dir.normalY(), pos.blockZ()+dir.normalZ());
                if (!oppositeBlock.isAir()) {
                    yield block.withProperty("attachment", "double_wall");
                } else {
                    yield block.withProperty("attachment", "single_wall");
                }
            }
        });
    }

}
