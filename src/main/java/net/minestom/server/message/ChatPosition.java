package net.minestom.server.message;

/**
 * The different positions for chat messages.
 */
public enum ChatPosition {
    /**
     * A player-initiated chat message.
     */
    CHAT,

    /**
     * Feedback from running a command or other system messages.
     */
    SYSTEM_MESSAGE,

    /**
     * Game state information displayed above the hot bar.
     */
    GAME_INFO;
}
