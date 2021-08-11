package net.minestom.server.listener;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

public class PlayerPositionListener {

    public static void playerPacketListener(ClientPlayerPacket packet, Player player) {
        player.refreshOnGround(packet.onGround);
    }

    public static void playerLookListener(ClientPlayerRotationPacket packet, Player player) {
        processMovement(player, player.getPosition().withView(packet.yaw, packet.pitch), packet.onGround);
    }

    public static void playerPositionListener(ClientPlayerPositionPacket packet, Player player) {
        processMovement(player, player.getPosition().withCoord(packet.x, packet.y, packet.z), packet.onGround);
    }

    public static void playerPositionAndLookListener(ClientPlayerPositionAndRotationPacket packet, Player player) {
        processMovement(player, new Pos(packet.x, packet.y, packet.z, packet.yaw, packet.pitch), packet.onGround);
    }

    public static void teleportConfirmListener(ClientTeleportConfirmPacket packet, Player player) {
        player.refreshReceivedTeleportId(packet.teleportId);
    }

    private static void processMovement(@NotNull Player player, @NotNull Pos newPosition, boolean onGround) {
        final var currentPosition = player.getPosition();
        if (currentPosition.equals(newPosition)) {
            // For some reason, the position is the same
            return;
        }
        final Instance instance = player.getInstance();
        // Prevent moving before the player spawned, probably a modified client (or high latency?)
        if (instance == null) {
            return;
        }
        // Prevent the player from moving during a teleport
        if (player.getLastSentTeleportId() != player.getLastReceivedTeleportId()) {
            return;
        }
        // Try to move in an unloaded chunk, prevent it
        if (!currentPosition.sameChunk(newPosition) && !ChunkUtils.isLoaded(instance, newPosition)) {
            player.teleport(currentPosition);
            return;
        }

        PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(player, newPosition);
        EventDispatcher.call(playerMoveEvent);
        // True if the event call changed the player position (possibly a teleport)
        if (!playerMoveEvent.isCancelled() && currentPosition.equals(player.getPosition())) {
            // Move the player
            player.refreshPosition(playerMoveEvent.getNewPosition());
            player.refreshOnGround(onGround);
        } else {
            // Cancelled, teleport to previous position
            player.teleport(player.getPosition());
        }
    }
}
