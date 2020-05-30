package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.network.packet.client.play.ClientPlayerPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionAndRotationPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerRotationPacket;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.chunk.ChunkUtils;

import java.util.function.Consumer;

public class PlayerPositionListener {

    public static void playerPacketListener(ClientPlayerPacket packet, Player player) {
        player.refreshOnGround(packet.onGround);
    }

    public static void playerLookListener(ClientPlayerRotationPacket packet, Player player) {
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

    public static void playerPositionAndLookListener(ClientPlayerPositionAndRotationPacket packet, Player player) {
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

        // Try to move in an unloaded chunk, prevent it
        if (ChunkUtils.isChunkUnloaded(player.getInstance(), x, z)) {
            player.teleport(player.getPosition());
            return;
        }

        PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(player, x, y, z, yaw, pitch);
        player.callEvent(PlayerMoveEvent.class, playerMoveEvent);
        if (!playerMoveEvent.isCancelled()) {
            consumer.accept(playerMoveEvent.getNewPosition());
        } else {
            player.teleport(player.getPosition());
        }
    }

}
