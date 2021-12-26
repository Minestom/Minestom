package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;

final class BlockPlaceMechanicDoorHinge {
    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        block = event.getBlock();

        final Point cursor = event.getCursorPosition();
        final String hinge = switch (block.getProperty("facing")) {
            case "west" -> cursor.z() > 0.5 ? "left" : "right";
            case "east" -> cursor.z() > 0.5 ? "right" : "left";
            case "south" -> cursor.x() > 0.5 ? "left" : "right";
            case "north" -> cursor.x() > 0.5 ? "right" : "left";
            default -> "left";
        };

        event.setBlock(block.withProperty("hinge", hinge));
    }
}
