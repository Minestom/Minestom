package net.minestom.server.listener;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerEditSignEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.client.play.ClientUpdateSignPacket;

public class EditSignListener {
    public static void listener(ClientUpdateSignPacket packet, Player player) {
        Instance instance = player.getInstance();
        BlockVec position = new BlockVec(packet.blockPosition());
        Block block = instance.getBlock(position);
        EventDispatcher.call(new PlayerEditSignEvent(
                player,
                instance,
                block,
                position,
                packet.lines(),
                packet.isFrontText()
        ));
    }
}
