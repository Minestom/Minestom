package net.minestom.server.listener.common;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;

public class PluginMessageListener {

    public static void listener(ClientPluginMessagePacket packet, Player player) {
        PlayerPluginMessageEvent pluginMessageEvent = new PlayerPluginMessageEvent(player, packet.channel(), packet.data());
        EventDispatcher.call(pluginMessageEvent);

        // Legacy (pre 1.13) clients have the outdated channel name.
        if (packet.channel().equals("MC|Brand") || packet.channel().equals("minecraft:brand")) {
            byte[] data = packet.data();
            int length = data.length;
            if (length == 0) {
                MinecraftServer.LOGGER.warn("Client {} sent an invalid brand plugin message", player.getUsername());
                return;
            }
            byte[] minusLength = new byte[data.length - 1];
            System.arraycopy(data, 1, minusLength, 0, minusLength.length);
            String brand = new String(minusLength).replace(" (Velocity)", ""); // Velocity adds this as a prefix to the brand
            player.refreshClientBrand(brand);
        }
    }

}
