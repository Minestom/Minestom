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
public final class PacketProcessor {
    private final ClientPacketsHandler statusPacketsHandler = new ClientPacketsHandler.Status();
    private final ClientPacketsHandler loginPacketsHandler = new ClientPacketsHandler.Login();
    private final ClientPacketsHandler playPacketsHandler = new ClientPacketsHandler.Play();

    public void process(@NotNull PlayerSocketConnection playerConnection, int packetId, ByteBuffer body) {
        if (MinecraftServer.getRateLimit() > 0) {
            // Increment packet count (checked in PlayerConnection#update)
            playerConnection.getPacketCounter().incrementAndGet();
        }
        BinaryReader binaryReader = new BinaryReader(body);
        final ConnectionState connectionState = playerConnection.getConnectionState();
        if (connectionState == ConnectionState.UNKNOWN) {
            // Should be handshake packet
            if (packetId == 0) {
                final HandshakePacket handshakePacket = new HandshakePacket(binaryReader);
                handshakePacket.process(playerConnection);
            }
            return;
        }
        switch (connectionState) {
            case PLAY -> {
                final Player player = playerConnection.getPlayer();
                ClientPacket playPacket = playPacketsHandler.createPacket(packetId, binaryReader);
                assert player != null;
                player.addPacketToQueue(playPacket);
            }
            case LOGIN -> {
                final ClientPreplayPacket loginPacket = (ClientPreplayPacket) loginPacketsHandler.createPacket(packetId, binaryReader);
                loginPacket.process(playerConnection);
            }
            case STATUS -> {
                final ClientPreplayPacket statusPacket = (ClientPreplayPacket) statusPacketsHandler.createPacket(packetId, binaryReader);
                statusPacket.process(playerConnection);
            }
        }
    }

    /**
     * Gets the handler for client status packets.
     *
     * @return the status packets handler
     * @see <a href="https://wiki.vg/Protocol#Status">Status packets</a>
     */
    public @NotNull ClientPacketsHandler statusPacketsHandler() {
        return statusPacketsHandler;
    }

    /**
     * Gets the handler for client login packets.
     *
     * @return the status login handler
     * @see <a href="https://wiki.vg/Protocol#Login">Login packets</a>
     */
    public @NotNull ClientPacketsHandler loginPacketsHandler() {
        return loginPacketsHandler;
    }

    /**
     * Gets the handler for client play packets.
     *
     * @return the play packets handler
     * @see <a href="https://wiki.vg/Protocol#Play">Play packets</a>
     */
    public @NotNull ClientPacketsHandler playPacketsHandler() {
        return playPacketsHandler;
    }
}
