package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;

final class BlockPlaceMechanicBell {
    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        block = event.getBlock();
        Direction dir = event.getBlockFace().toDirection();

        String attachment = switch (dir) {
            case UP -> "floor";
            case DOWN -> "ceiling";
            case NORTH, SOUTH, WEST, EAST -> {
                final Point pos = event.getBlockPosition();
                final Block oppositeBlock = event.getInstance().getBlock(pos.blockX() + dir.normalX(),
                        pos.blockY() + dir.normalY(), pos.blockZ() + dir.normalZ());
                yield oppositeBlock.isAir() ? "single_wall" : "double_wall";
            }
        };
        event.setBlock(block.withProperty("attachment", attachment));
    }
}
