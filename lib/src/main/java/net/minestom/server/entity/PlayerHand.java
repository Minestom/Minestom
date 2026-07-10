package net.minestom.server.entity;

import net.minestom.server.network.NetworkBuffer;

/**
 * Represents the main or off hand of the player.
 */
public enum PlayerHand {
    MAIN,
    OFF;

    public static final NetworkBuffer.Type<PlayerHand> NETWORK_TYPE = NetworkBuffer.Enum(PlayerHand.class);
}
