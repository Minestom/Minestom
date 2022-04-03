package net.minestom.server.message

import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.network.packet.server.play.ChatMessagePacket
import net.minestom.server.message.Messenger
import net.minestom.server.message.ChatPosition
import net.minestom.server.utils.PacketUtils
import net.minestom.server.message.ChatMessageType
import java.lang.IllegalArgumentException
import java.util.EnumSet

/**
 * The different positions for chat messages.
 */
enum class ChatPosition(
    /**
     * Gets the Adventure message type from this position. Note that there is no
     * message type for [.GAME_INFO], as Adventure uses the title methods for this.
     *
     * @return the message type, if any
     */
    val messageType: MessageType?
) {
    /**
     * A player-initiated chat message.
     */
    CHAT(MessageType.CHAT),

    /**
     * Feedback from running a command or other system messages.
     */
    SYSTEM_MESSAGE(MessageType.SYSTEM),

    /**
     * Game state information displayed above the hot bar.
     */
    GAME_INFO(null);

    /**
     * Gets the packet ID of this chat position.
     *
     * @return the ID
     */
    val iD: Byte
        get() = ordinal.toByte()

    companion object {
        /**
         * Gets a position from an Adventure message type.
         *
         * @param messageType the message type
         * @return the position
         */
        @JvmStatic
        fun fromMessageType(messageType: MessageType): ChatPosition {
            return when (messageType) {
                MessageType.CHAT -> CHAT
                MessageType.SYSTEM -> SYSTEM_MESSAGE
            }
        }

        /**
         * Gets a position from a packet ID.
         *
         * @param id the id
         * @return the chat position
         */
        @JvmStatic
        fun fromPacketID(id: Byte): ChatPosition {
            return when (id) {
                0 -> CHAT
                1 -> SYSTEM_MESSAGE
                2 -> GAME_INFO
                else -> throw IllegalArgumentException("id must be between 0-2 (inclusive)")
            }
        }
    }
}