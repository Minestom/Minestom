package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockActions;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.server.network.packet.server.play.AcknowledgeBlockChangePacket;

public class BlockPlacementListener {

    public static void listener(ClientPlayerBlockPlacementPacket packet, Player player) {
        final Instance instance = player.getInstance();
        if (instance == null) {
            player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
            return;
        }
        BlockActions.place(BlockActions.world(instance), player, packet);
    }
}
