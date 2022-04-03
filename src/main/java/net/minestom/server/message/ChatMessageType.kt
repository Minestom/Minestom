package net.minestom.server.message

import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.network.packet.server.play.ChatMessagePacket
import net.minestom.server.message.Messenger
import net.minestom.server.message.ChatPosition
import net.minestom.server.utils.PacketUtils
import net.minestom.server.message.ChatMessageType
import java.lang.IllegalArgumentException
import java.util.EnumSet

/**
 * The messages that a player is willing to receive.
 */
enum class ChatMessageType(private val acceptedPositions: EnumSet<ChatPosition>) {
    /**
     * The client wants all chat messages.
     */
    FULL(EnumSet.allOf(ChatPosition::class.java)),

    /**
     * The client only wants messages from commands, or system messages.
     */
    SYSTEM(EnumSet.of(ChatPosition.SYSTEM_MESSAGE, ChatPosition.GAME_INFO)),

    /**
     * The client doesn't want any messages.
     */
    NONE(EnumSet.of(ChatPosition.GAME_INFO));

    /**
     * Checks if this message type is accepting of messages from a given position.
     *
     * @param chatPosition the position
     * @return if the message is accepted
     */
    fun accepts(chatPosition: ChatPosition): Boolean {
        return acceptedPositions.contains(chatPosition)
    }

    /**
     * Gets the packet ID for this chat message type.
     *
     * @return the packet ID
     */
    fun getPacketID(): Int {
        return ordinal
    }

    companion object {
        /**
         * Gets a chat message type from a packet ID.
         *
         * @param id the packet ID
         * @return the chat message type
         */
        @JvmStatic
        fun fromPacketID(id: Int): ChatMessageType {
            return when (id) {
                0 -> FULL
                1 -> SYSTEM
                2 -> NONE
                else -> throw IllegalArgumentException("id must be between 0-2 (inclusive)")
            }
        }
    }
}