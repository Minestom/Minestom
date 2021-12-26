package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.network.packet.server.play.BlockChangePacket;

class BlockPlaceMechanicUpper {

    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        Point position = event.getBlockPosition();

        if ("lower".equals(block.getProperty("half"))) {
            Point abovePosition = position.add(0, 1, 0);

            Block aboveBlock = event.getInstance().getBlock(abovePosition);
            if (aboveBlock.isAir()) {
                // TODO: Fire in execution context after the current event
                PlayerBlockPlaceEvent playerBlockPlaceEvent = new PlayerBlockPlaceEvent(event.getPlayer(),
                        block.withProperty("half", "upper"), BlockFace.TOP, abovePosition,
                        event.getCursorPosition(), event.getHand());
                EventDispatcher.call(playerBlockPlaceEvent);

                if (playerBlockPlaceEvent.isCancelled()) {
                    event.getPlayer().getPlayerConnection().sendPacket(new BlockChangePacket(abovePosition, aboveBlock));
                } else {
                    event.getInstance().setBlock(abovePosition, playerBlockPlaceEvent.getBlock());
                }
            }
        }
    }

}
