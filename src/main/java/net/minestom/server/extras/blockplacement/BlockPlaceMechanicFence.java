package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerBlockUpdateNeighborEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.Map;

final class BlockPlaceMechanicFence {
    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        event.setBlock(update(event.getBlock(), event.getInstance(), event.getBlockPosition()));
    }

    static void onNeighbor(Block block, PlayerBlockUpdateNeighborEvent event) {
        event.setBlock(update(event.getBlock(), event.getInstance(), event.getBlockPosition()));
    }

    private static Block update(Block state, Instance instance, Point position) {
        final boolean northNeighbor = instance.getBlock(position.blockX(), position.blockY(), position.blockZ() - 1).isSolid();
        final boolean southNeighbor = instance.getBlock(position.blockX(), position.blockY(), position.blockZ() + 1).isSolid();
        final boolean eastNeighbor = instance.getBlock(position.blockX() + 1, position.blockY(), position.blockZ()).isSolid();
        final boolean westNeighbor = instance.getBlock(position.blockX() - 1, position.blockY(), position.blockZ()).isSolid();
        return state.withProperties(Map.of(
                "north", String.valueOf(northNeighbor),
                "south", String.valueOf(southNeighbor),
                "east", String.valueOf(eastNeighbor),
                "west", String.valueOf(westNeighbor)
        ));
    }
}
