package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerMoveEvent;
import net.minestom.server.network.packet.client.play.ClientPlayerLookPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionAndLookPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket;
import net.minestom.server.utils.ChunkUtils;
import net.minestom.server.utils.Position;

import java.util.function.Consumer;

public class PlayerPositionListener {

    public static void playerPacketListener(ClientPlayerPacket packet, Player player) {
        player.refreshOnGround(packet.onGround);
    }

    public static void playerLookListener(ClientPlayerLookPacket packet, Player player) {
        Position playerPosition = player.getPosition();
        float x = playerPosition.getX();
        float y = playerPosition.getY();
        float z = playerPosition.getZ();
        float yaw = packet.yaw;
        float pitch = packet.pitch;
        processMovement(player, x, y, z, yaw, pitch, (position) -> {
            player.refreshPosition(position.getX(), position.getY(), position.getZ());
            player.refreshView(position.getYaw(), position.getPitch());
            player.refreshOnGround(packet.onGround);
        });
    }

    public static void playerPositionListener(ClientPlayerPositionPacket packet, Player player) {
        Position playerPosition = player.getPosition();
        float x = (float) packet.x;
        float y = (float) packet.y;
        float z = (float) packet.z;
        float yaw = playerPosition.getYaw();
        float pitch = playerPosition.getPitch();
        processMovement(player, x, y, z, yaw, pitch, (position) -> {
            player.refreshPosition(position.getX(), position.getY(), position.getZ());
            player.refreshView(position.getYaw(), position.getPitch());
            player.refreshOnGround(packet.onGround);
        });
    }

    public static void playerPositionAndLookListener(ClientPlayerPositionAndLookPacket packet, Player player) {
        float x = (float) packet.x;
        float y = (float) packet.y;
        float z = (float) packet.z;
        float yaw = packet.yaw;
        float pitch = packet.pitch;
        processMovement(player, x, y, z, yaw, pitch, (position) -> {
            player.refreshPosition(position.getX(), position.getY(), position.getZ());
            player.refreshView(position.getYaw(), position.getPitch());
            player.refreshOnGround(packet.onGround);
        });
    }

    private static void processMovement(Player player, float x, float y, float z,
                                        float yaw, float pitch, Consumer<Position> consumer) {

        boolean chunkTest = ChunkUtils.isChunkUnloaded(player.getInstance(), x, z);
        if (chunkTest) {
            player.teleport(player.getPosition());
            return;
        }

        PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(x, y, z, yaw, pitch);
        player.callEvent(PlayerMoveEvent.class, playerMoveEvent);
        if (!playerMoveEvent.isCancelled()) {
            consumer.accept(playerMoveEvent.getNewPosition());
        } else {
            player.teleport(player.getPosition());
        }
    }

}
