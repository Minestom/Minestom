package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerBlockUpdateNeighborEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class BlockPlaceMechanicFence {

    public static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        event.setBlock(update(event.getBlock(), event.getInstance(), event.getBlockPosition()));
    }

    public static void onNeighbor(Block block, PlayerBlockUpdateNeighborEvent event) {
        event.setBlock(update(event.getBlock(), event.getInstance(), event.getBlockPosition()));
    }

    private static Block update(Block state, Instance instance, Point position) {
        boolean northNeighbor = instance.getBlock(position.blockX(), position.blockY(), position.blockZ()-1).isSolid();
        boolean southNeighbor = instance.getBlock(position.blockX(), position.blockY(), position.blockZ()+1).isSolid();
        boolean eastNeighbor = instance.getBlock(position.blockX()+1, position.blockY(), position.blockZ()).isSolid();
        boolean westNeighbor = instance.getBlock(position.blockX()-1, position.blockY(), position.blockZ()).isSolid();

        state = state.withProperty("north", northNeighbor ? "true" : "false");
        state = state.withProperty("south", southNeighbor ? "true" : "false");
        state = state.withProperty("east", eastNeighbor ? "true" : "false");
        state = state.withProperty("west", westNeighbor ? "true" : "false");

        return state;
    }
}
