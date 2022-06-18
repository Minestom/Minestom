package net.minestom.server.message;

import org.jetbrains.annotations.NotNull;

/**
 * The messages that a player is willing to receive.
 */
public enum ChatPreference {
    /**
     * The client wants all chat messages.
     */
    FULL,

    /**
     * The client only wants messages from commands, or system messages.
     */
    SYSTEM,

    /**
     * The client doesn't want any messages.
     */
    NONE;

    /**
     * Gets the packet ID for this chat message type.
     *
     * @return the packet ID
     */
    public int getPacketID() {
        return this.ordinal();
    }

    /**
     * Gets a chat message type from a packet ID.
     *
     * @param id the packet ID
     * @return the chat message type
     */
    public static @NotNull ChatPreference fromPacketID(int id) {
        return switch (id) {
            case 0 -> FULL;
            case 1 -> SYSTEM;
            case 2 -> NONE;
            default -> throw new IllegalArgumentException("id must be between 0-2 (inclusive)");
        };
    }
}
