package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerBlockUpdateNeighborEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

final class BlockPlaceMechanicPointedDripstone {
    static void onNeighbor(Block block, PlayerBlockUpdateNeighborEvent event) {
        String direction = block.getProperty("vertical_direction");
        if (direction == null) return;

        Instance instance = event.getInstance();
        Point position = event.getBlockPosition();

        Block aboveState = instance.getBlock(position.blockX(), position.blockY() + (direction.equals("down") ? -1 : 1), position.blockZ());

        if (Block.POINTED_DRIPSTONE.compare(aboveState, Block.Comparator.ID)) {
            String thickness = aboveState.getProperty("thickness");

            if (aboveState.getProperty("vertical_direction").equals(direction)) {
                switch (thickness) {
                    case "tip", "tip_merge" -> block = block.withProperty("thickness", "frustum");
                    case "frustum", "middle" -> {
                        Block blockOpposite = instance.getBlock(position.blockX(), position.blockY() + (direction.equals("down") ? 1 : -1), position.blockZ());
                        if (!Block.POINTED_DRIPSTONE.compare(blockOpposite, Block.Comparator.ID)) {
                            block = block.withProperty("thickness", "base");
                        } else {
                            block = block.withProperty("thickness", "middle");
                        }
                    }
                }
            } else {
                switch (thickness) {
                    case "tip", "tip_merge" -> block = block.withProperty("thickness", "tip_merge");
                }
            }
        } else {
            block = block.withProperty("thickness", "tip");
        }

        event.setShouldUpdateNeighbors(true);
        event.setBlock(block);
    }

    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        BlockPlaceMechanicHalf.onPlace(block, event, "vertical_direction", "down", "up");
        block = event.getBlock();

        String direction = block.getProperty("vertical_direction");
        if (direction == null) return;

        Instance instance = event.getInstance();
        Point position = event.getBlockPosition();

        Block aboveState = instance.getBlock(position.blockX(), position.blockY() + (direction.equals("down") ? -1 : 1), position.blockZ());

        if (Block.POINTED_DRIPSTONE.compare(aboveState, Block.Comparator.ID)) {
            String thickness = aboveState.getProperty("thickness");

            if (!aboveState.getProperty("vertical_direction").equals(direction)) {
                switch (thickness) {
                    case "tip", "tip_merge" -> block = block.withProperty("thickness", "tip_merge");
                }
            }
        }

        event.setBlock(block);
    }
}
