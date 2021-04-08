package net.minestom.server.ping;

import org.jetbrains.annotations.Nullable;

public final class HandshakeData {
    private final @Nullable String serverAddress;
    private final int serverPort;
    private final int protocolVersion;

    public HandshakeData(@Nullable String serverAddress, int serverPort, int protocolVersion) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.protocolVersion = protocolVersion;
    }


    /**
     * Get the server address a client used to connect.
     * may be null
     * @return the server address
     */
    public @Nullable String getServerAddress() {
        return serverAddress;
    }

    /**
     * Get the server port a client used to connect.
     *
     * @return the server port
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Get the protocol version a client used to connect.
     *
     * @return the protocol version
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }


}
