package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.PlayerMoveEvent;
import fr.themode.minestom.net.packet.client.play.ClientPlayerLookPacket;
import fr.themode.minestom.net.packet.client.play.ClientPlayerPacket;
import fr.themode.minestom.net.packet.client.play.ClientPlayerPositionAndLookPacket;
import fr.themode.minestom.net.packet.client.play.ClientPlayerPositionPacket;
import fr.themode.minestom.utils.ChunkUtils;

public class PlayerPositionListener {

    public static void playerPacketListener(ClientPlayerPacket packet, Player player) {
        player.refreshOnGround(packet.onGround);
    }

    public static void playerLookListener(ClientPlayerLookPacket packet, Player player) {
        player.refreshView(packet.yaw, packet.pitch);
        player.refreshOnGround(packet.onGround);
        // TODO move event?
    }

    public static void playerPositionListener(ClientPlayerPositionPacket packet, Player player) {
        float x = (float) packet.x;
        float y = (float) packet.y;
        float z = (float) packet.z;
        processMovement(player, x, y, z, () -> {
            player.refreshPosition(x, y, z);
            player.refreshOnGround(packet.onGround);
        });
    }

    public static void playerPositionAndLookListener(ClientPlayerPositionAndLookPacket packet, Player player) {
        float x = (float) packet.x;
        float y = (float) packet.y;
        float z = (float) packet.z;
        processMovement(player, x, y, z, () -> {
            player.refreshPosition(x, y, z);
            player.refreshView(packet.yaw, packet.pitch);
            player.refreshOnGround(packet.onGround);
        });
    }

    private static void processMovement(Player player, float x, float y, float z, Runnable runnable) {
        //System.out.println("MOVEMENT PACKET " + Math.round(x) + ":" + Math.round(y) + ":" + Math.round(z));
        boolean chunkTest = ChunkUtils.isChunkUnloaded(player.getInstance(), x, z);
        if (chunkTest) {
            player.teleport(player.getPosition());
            return;
        }

        PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(x, y, z);
        player.callEvent(PlayerMoveEvent.class, playerMoveEvent);
        if (!playerMoveEvent.isCancelled()) {
            runnable.run();
        } else {
            player.teleport(player.getPosition());
        }
    }

}
