package net.minestom.server.instance.block;

import net.minestom.server.network.NetworkBuffer;

/**
 * Which of the two text faces of a sign is being read or written.
 */
public enum SignTextSlot {
    BACK,
    FRONT;

    public static final NetworkBuffer.Type<SignTextSlot> NETWORK_TYPE = NetworkBuffer.Enum(SignTextSlot.class);
}
