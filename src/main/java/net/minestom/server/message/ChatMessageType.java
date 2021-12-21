package net.minestom.server.message;

import org.jetbrains.annotations.NotNull;

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

    ChatMessageType(@NotNull EnumSet<ChatPosition> acceptedPositions) {
        this.acceptedPositions = acceptedPositions;
    }

    /**
     * Checks if this message type is accepting of messages from a given position.
     *
     * @param chatPosition the position
     * @return if the message is accepted
     */
    public boolean accepts(@NotNull ChatPosition chatPosition) {
        return this.acceptedPositions.contains(chatPosition);
    }

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
    public static @NotNull ChatMessageType fromPacketID(int id) {
        return switch (id) {
            case 0 -> FULL;
            case 1 -> SYSTEM;
            case 2 -> NONE;
            default -> throw new IllegalArgumentException("id must be between 0-2 (inclusive)");
        };
    }
}
