package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatPreviewEvent;
import net.minestom.server.network.packet.client.play.ClientChatPreviewPacket;
import net.minestom.server.network.packet.server.play.ChatPreviewPacket;

public class ChatPreviewListener {
    public static void listener(ClientChatPreviewPacket packet, Player player) {
        final PlayerChatPreviewEvent event = new PlayerChatPreviewEvent(player, packet.queryId(), packet.query());
        MinecraftServer.getGlobalEventHandler().callCancellable(event,
                () -> player.sendPacket(new ChatPreviewPacket(event.getId(), event.getResult())));
    }
}
