package net.minestom.server.listener.common;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.ListenerHandle;
import net.minestom.server.event.player.PlayerBrandEvent;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;

public class PluginMessageListener {
    private static final String BRAND_CHANNEL = "minecraft:brand";
    private static final ListenerHandle<PlayerBrandEvent> brandHandle = EventDispatcher.getHandle(PlayerBrandEvent.class);

    public static void listener(ClientPluginMessagePacket packet, Player player) {
        PlayerPluginMessageEvent pluginMessageEvent = new PlayerPluginMessageEvent(player, packet.channel(), packet.data());
        EventDispatcher.call(pluginMessageEvent);

        if (brandHandle.hasListener() && BRAND_CHANNEL.equals(pluginMessageEvent.getIdentifier())) {
            NetworkBuffer buffer = NetworkBuffer.wrap(packet.data(), 0, packet.data().length);
            EventDispatcher.call(new PlayerBrandEvent(player, buffer.read(NetworkBuffer.STRING)));
        }
    }

}
