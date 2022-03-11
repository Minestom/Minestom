package net.minestom.server.listener;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.server.play.PlayerPositionAndLookPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

public class PlayerPositionListener {

    public static void playerPacketListener(ClientPlayerPacket packet, Player player) {
        player.refreshOnGround(packet.onGround());
    }

    public static void playerLookListener(ClientPlayerRotationPacket packet, Player player) {
        processMovement(player, player.getPosition().withView(packet.yaw(), packet.pitch()), packet.onGround());
    }

    public static void playerPositionListener(ClientPlayerPositionPacket packet, Player player) {
        processMovement(player, player.getPosition().withCoord(packet.position()), packet.onGround());
    }

    public static void playerPositionAndLookListener(ClientPlayerPositionAndRotationPacket packet, Player player) {
        processMovement(player, packet.position(), packet.onGround());
    }

    public static void teleportConfirmListener(ClientTeleportConfirmPacket packet, Player player) {
        player.refreshReceivedTeleportId(packet.teleportId());
    }

    private static void processMovement(@NotNull Player player, @NotNull Pos packetPosition, boolean onGround) {
        final var currentPosition = player.getPosition();
        if (currentPosition.equals(packetPosition)) {
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
        if (!currentPosition.sameChunk(packetPosition) && !ChunkUtils.isLoaded(instance, packetPosition)) {
            player.teleport(currentPosition);
            return;
        }

        PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(player, packetPosition);
        EventDispatcher.call(playerMoveEvent);
        if (!currentPosition.equals(player.getPosition())) {
            // Player has been teleported in the event
            return;
        }
        if (playerMoveEvent.isCancelled()) {
            // Teleport to previous position
            PlayerConnection connection = player.getPlayerConnection();
            connection.sendPacket(new PlayerPositionAndLookPacket(currentPosition, (byte) 0x00, player.getNextTeleportId(), false));
            return;
        }
        final Pos eventPosition = playerMoveEvent.getNewPosition();
        if (packetPosition.equals(eventPosition)) {
            // Event didn't change the position
            player.refreshPosition(eventPosition);
            player.refreshOnGround(onGround);
        } else {
            // Position modified by the event
            if (packetPosition.samePoint(eventPosition)) {
                player.refreshPosition(eventPosition, true);
                player.refreshOnGround(onGround);
                player.setView(eventPosition.yaw(), eventPosition.pitch());
            } else {
                player.teleport(eventPosition);
            }
        }
    }
}
