package net.minestom.server.network;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.ClientPacketsHandler;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.client.handshake.HandshakePacket;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Responsible for processing client packets.
 * <p>
 * You can retrieve the different packet handlers per state (status/login/play)
 * from the {@link ClientPacketsHandler} classes.
 */
public record PacketProcessor(@NotNull ClientPacketsHandler statusHandler,
                              @NotNull ClientPacketsHandler loginHandler,
                              @NotNull ClientPacketsHandler playHandler) {
    public PacketProcessor() {
        this(new ClientPacketsHandler.Status(),
                new ClientPacketsHandler.Login(),
                new ClientPacketsHandler.Play());
    }

    public void process(@NotNull PlayerSocketConnection connection, int packetId, ByteBuffer body) {
        if (MinecraftServer.getRateLimit() > 0) {
            // Increment packet count (checked in PlayerConnection#update)
            connection.getPacketCounter().incrementAndGet();
        }
        BinaryReader binaryReader = new BinaryReader(body);
        final ConnectionState connectionState = connection.getConnectionState();
        if (connectionState == ConnectionState.UNKNOWN) {
            // Should be handshake packet
            if (packetId == 0) {
                final HandshakePacket handshakePacket = new HandshakePacket(binaryReader);
                handshakePacket.process(connection);
            }
            return;
        }
        switch (connectionState) {
            case PLAY -> {
                final Player player = connection.getPlayer();
                ClientPacket playPacket = playHandler.createPacket(packetId, binaryReader);
                assert player != null;
                player.addPacketToQueue(playPacket);
            }
            case LOGIN -> {
                final ClientPreplayPacket loginPacket = (ClientPreplayPacket) loginHandler.createPacket(packetId, binaryReader);
                loginPacket.process(connection);
            }
            case STATUS -> {
                final ClientPreplayPacket statusPacket = (ClientPreplayPacket) statusHandler.createPacket(packetId, binaryReader);
                statusPacket.process(connection);
            }
        }
    }
}
