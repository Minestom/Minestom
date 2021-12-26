package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerBlockUpdateNeighborEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.Map;

final class BlockPlaceMechanicWall {
    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        event.setBlock(update(event.getBlock(), event.getBlockPosition(), event.getInstance()));
    }

    static void onNeighbor(Block block, PlayerBlockUpdateNeighborEvent event) {
        event.setBlock(update(event.getBlock(), event.getBlockPosition(), event.getInstance()));
        event.setShouldUpdateNeighbors(true);
    }

    private static Block update(Block block, Point position, Instance instance) {
        boolean northNeighbor = instance.getBlock(position.blockX(), position.blockY(), position.blockZ() - 1).isSolid();
        boolean southNeighbor = instance.getBlock(position.blockX(), position.blockY(), position.blockZ() + 1).isSolid();
        boolean eastNeighbor = instance.getBlock(position.blockX() + 1, position.blockY(), position.blockZ()).isSolid();
        boolean westNeighbor = instance.getBlock(position.blockX() - 1, position.blockY(), position.blockZ()).isSolid();

        block = block.withProperty("up", "" + !((northNeighbor && southNeighbor) || (eastNeighbor && westNeighbor)));

        Block aboveBlock = instance.getBlock(position.blockX(), position.blockY() + 1, position.blockZ());

        if (PlacementRules.isWall(aboveBlock)) {
            if (Boolean.parseBoolean(aboveBlock.getProperty("up"))) {
                block = block.withProperty("up", "true");
            } else {
                block = block.withProperty("up", "" + !((northNeighbor && southNeighbor &&
                        !"none".equals(aboveBlock.getProperty("north")) && !"none".equals(aboveBlock.getProperty("south")))
                        || (eastNeighbor && westNeighbor && !"none".equals(aboveBlock.getProperty("east")) && !"none".equals(aboveBlock.getProperty("west")))));
            }
        } else {
            block = block.withProperty("up", "" + !((northNeighbor && southNeighbor && eastNeighbor && westNeighbor) ||
                    (!northNeighbor && !southNeighbor && eastNeighbor && westNeighbor) ||
                    (northNeighbor && southNeighbor && !eastNeighbor && !westNeighbor)));
        }

        boolean above = aboveBlock.isSolid();

        if (above) {
            block = block.withProperties(Map.of(
                    "north", northNeighbor ? (instance.getBlock(position.blockX(), position.blockY() + 1, position.blockZ() - 1).isSolid() ? "tall" : "low") : "none",
                    "south", southNeighbor ? (instance.getBlock(position.blockX(), position.blockY() + 1, position.blockZ() + 1).isSolid() ? "tall" : "low") : "none",
                    "east", eastNeighbor ? (instance.getBlock(position.blockX() + 1, position.blockY() + 1, position.blockZ()).isSolid() ? "tall" : "low") : "none",
                    "west", westNeighbor ? (instance.getBlock(position.blockX() - 1, position.blockY() + 1, position.blockZ()).isSolid() ? "tall" : "low") : "none"
            ));
        } else {
            block = block.withProperties(Map.of(
                    "north", northNeighbor ? "low" : "none",
                    "south", southNeighbor ? "low" : "none",
                    "east", eastNeighbor ? "low" : "none",
                    "west", westNeighbor ? "low" : "none"
            ));
        }

        return block;
    }
}
