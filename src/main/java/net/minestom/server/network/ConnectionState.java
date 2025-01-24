package net.minestom.server.network;

/**
 * Represents the connection state of a client.
 */
public enum ConnectionState {
    /**
     * Default state before any packet is received.
     */
    HANDSHAKE,
    /**
     * Client declares `Status` intent during handshake.
     */
    STATUS,
    /**
     * Client declares `Login` intent during handshake.
     */
    LOGIN,
    /**
     * Client acknowledged login and is now configuring the game.
     * Can also go back to configuration from play.
     */
    CONFIGURATION,
    /**
     * Client (re-)finished configuration.
     */
    PLAY
}
