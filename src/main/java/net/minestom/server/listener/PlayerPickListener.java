package net.minestom.server.listener;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerPickBlockEvent;
import net.minestom.server.event.player.PlayerPickEntityEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.client.play.ClientPickItemFromBlockPacket;
import net.minestom.server.network.packet.client.play.ClientPickItemFromEntityPacket;

public class PlayerPickListener {

    public static void playerPickBlockListener(ClientPickItemFromBlockPacket packet, Player player) {
        final Instance instance = player.getInstance();
        if (instance == null) return;
        final Block block = instance.getBlock(packet.pos());
        if (block.isAir()) return;
        final boolean includeData = packet.includeData();

        PlayerPickBlockEvent playerPickBlockEvent = new PlayerPickBlockEvent(player, block, new BlockVec(packet.pos()), includeData);
        EventDispatcher.call(playerPickBlockEvent);
    }

    public static void playerPickEntityListener(ClientPickItemFromEntityPacket packet, Player player) {
        final Instance instance = player.getInstance();
        if (instance == null) return;
        final Entity entity = instance.getEntityById(packet.entityId());
        if (entity == null) return;
        final boolean includeData = packet.includeData();

        PlayerPickEntityEvent playerPickEntityEvent = new PlayerPickEntityEvent(player, entity, includeData);
        EventDispatcher.call(playerPickEntityEvent);
    }
}
