package net.minestom.server.event.server;

import net.minestom.server.event.Event;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Called by a HandshakePacket.
 */
public class HandshakeEvent extends Event {

    private final String serverAddress;
    private final int serverPort;
    private final int protocolVersion;
    private final PlayerConnection connection;

    public HandshakeEvent(@Nullable String serverAddress, int serverPort, int protocolVersion, @NotNull PlayerConnection connection) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.protocolVersion = protocolVersion;
        this.connection = connection;
    }

    /**
     * @return the server address a client used to connect to the server
     */
    public @Nullable String getServerAddress() {
        return serverAddress;
    }
    /**
     * @return the server port a client used to connect to the server
     */
    public int getServerPort() {
        return serverPort;
    }
    /**
     * @return the protocol version a client used when connecting to the server
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }
    /**
     * @return the player connection
     */
    public @NotNull PlayerConnection getConnection() {
        return connection;
    }
}
