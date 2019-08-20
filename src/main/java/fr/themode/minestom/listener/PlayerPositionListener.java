package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.play.ClientPlayerLookPacket;
import fr.themode.minestom.net.packet.client.play.ClientPlayerPacket;
import fr.themode.minestom.net.packet.client.play.ClientPlayerPositionAndLookPacket;
import fr.themode.minestom.net.packet.client.play.ClientPlayerPositionPacket;

public class PlayerPositionListener {

    public static void playerPacketListener(ClientPlayerPacket packet, Player player) {
        player.refreshOnGround(packet.onGround);
    }

    public static void playerLookListener(ClientPlayerLookPacket packet, Player player) {
        player.refreshView(packet.yaw, packet.pitch);
        player.refreshOnGround(packet.onGround);
    }

    public static void playerPositionListener(ClientPlayerPositionPacket packet, Player player) {
        boolean chunkTest = player.chunkTest(packet.x, packet.z);
        if (chunkTest) {
            player.teleport(player.getX(), player.getY(), player.getZ());
            return;
        }

        player.refreshPosition(packet.x, packet.y, packet.z);
        player.refreshOnGround(packet.onGround);
    }

    public static void playerPositionAndLookListener(ClientPlayerPositionAndLookPacket packet, Player player) {
        boolean chunkTest = player.chunkTest(packet.x, packet.z);
        if (chunkTest) {
            player.teleport(player.getX(), player.getY(), player.getZ());
            return;
        }

        player.refreshPosition(packet.x, packet.y, packet.z);
        player.refreshView(packet.yaw, packet.pitch);
        player.refreshOnGround(packet.onGround);
    }

}
