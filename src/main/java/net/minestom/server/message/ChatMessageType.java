package net.minestom.server.message;

import java.util.EnumSet;

/**
 * The messages that a player is willing to receive.
 */
public enum ChatMessageType {
    /**
     * The client wants all chat messages.
     */
    FULL(EnumSet.allOf(ChatPosition.class)),

    /**
     * The client only wants messages from commands, or system messages.
     */
    SYSTEM(EnumSet.of(ChatPosition.SYSTEM_MESSAGE, ChatPosition.GAME_INFO)),

    /**
     * The client doesn't want any messages.
     */
    NONE(EnumSet.of(ChatPosition.GAME_INFO));

    private final EnumSet<ChatPosition> acceptedPositions;

    ChatMessageType(EnumSet<ChatPosition> acceptedPositions) {
        this.acceptedPositions = acceptedPositions;
    }

    /**
     * Checks if this message type is accepting of messages from a given position.
     *
     * @param chatPosition the position
     * @return if the message is accepted
     */
    public boolean accepts(ChatPosition chatPosition) {
        return this.acceptedPositions.contains(chatPosition);
    }
}
