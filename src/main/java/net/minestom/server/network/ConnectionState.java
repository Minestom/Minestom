package net.minestom.server.network;

import net.minestom.server.entity.Player;

/**
 * Represent the current connection state of a {@link Player}
 */
public enum ConnectionState {
    UNKNOWN, STATUS, LOGIN, PLAY
}
