package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

public class PlayerPositionListener {

    public static void playerPacketListener(ClientPlayerPacket packet, Player player) {
        player.refreshOnGround(packet.onGround);
    }

    public static void playerLookListener(ClientPlayerRotationPacket packet, Player player) {
        final Position playerPosition = player.getPosition();
        final double x = playerPosition.getX();
        final double y = playerPosition.getY();
        final double z = playerPosition.getZ();
        final float yaw = packet.yaw;
        final float pitch = packet.pitch;
        final boolean onGround = packet.onGround;
        processMovement(player, x, y, z, yaw, pitch, onGround);
    }

    public static void playerPositionListener(ClientPlayerPositionPacket packet, Player player) {
        final Position playerPosition = player.getPosition();
        final float yaw = playerPosition.getYaw();
        final float pitch = playerPosition.getPitch();
        final boolean onGround = packet.onGround;
        processMovement(player,
                packet.x, packet.y, packet.z,
                yaw, pitch, onGround);
    }

    public static void playerPositionAndLookListener(ClientPlayerPositionAndRotationPacket packet, Player player) {
        final float yaw = packet.yaw;
        final float pitch = packet.pitch;
        final boolean onGround = packet.onGround;
        processMovement(player,
                packet.x, packet.y, packet.z,
                yaw, pitch, onGround);
    }

    public static void teleportConfirmListener(ClientTeleportConfirmPacket packet, Player player) {
        final int packetTeleportId = packet.teleportId;
        System.out.println("Received teleport id packet: " + packet.teleportId);
        player.refreshReceivedTeleportId(packetTeleportId);
    }

    private static void processMovement(@NotNull Player player, double x, double y, double z,
                                        float yaw, float pitch, boolean onGround) {
        final Instance instance = player.getInstance();

        System.out.println("Move packet received: " + x + ":" + y + ":" + z);

        // Prevent moving before the player spawned, probably a modified client (or high latency?)
        if (instance == null) {
            return;
        }

        // Prevent the player from moving during a teleport
        if (player.getLastSentTeleportId() != player.getLastReceivedTeleportId()) {
            return;
        }

        // Try to move in an unloaded chunk, prevent it
        if (!ChunkUtils.isLoaded(instance, x, z)) {
            player.teleport(player.getPosition());
            return;
        }

        final Position currentPosition = player.getPosition().clone();
        Position newPosition = new Position(x, y, z, yaw, pitch);
        final Position cachedPosition = newPosition.clone();

        PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(player, newPosition);
        EventDispatcher.call(playerMoveEvent);

        // True if the event call changed the player position (possibly a teleport)
        final boolean positionChanged = !currentPosition.equals(player.getPosition());

        if (!playerMoveEvent.isCancelled() && !positionChanged) {
            // Move the player
            newPosition = playerMoveEvent.getNewPosition();
            if (!newPosition.equals(cachedPosition)) {
                // New position from the event changed, teleport the player
                player.teleport(newPosition);
            }
            // Change the internal data
            player.refreshPosition(newPosition);
            player.refreshOnGround(onGround);
        } else {
            player.teleport(player.getPosition());
        }
    }

}
