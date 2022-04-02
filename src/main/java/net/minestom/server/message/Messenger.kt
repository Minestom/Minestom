package net.minestom.server.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.ChatMessagePacket
import net.minestom.server.message.Messenger
import net.minestom.server.message.ChatPosition
import net.minestom.server.utils.PacketUtils
import net.minestom.server.message.ChatMessageType
import java.lang.IllegalArgumentException
import java.util.*

/**
 * Utility class to handle client chat settings.
 */
object Messenger {
    /**
     * The message sent to the client if they send a chat message but it is rejected by the server.
     */
    val CANNOT_SEND_MESSAGE: Component = Component.translatable("chat.cannotSend", NamedTextColor.RED)
    private val CANNOT_SEND_PACKET = ChatMessagePacket(CANNOT_SEND_MESSAGE, ChatPosition.SYSTEM_MESSAGE, null)

    /**
     * Sends a message to a player, respecting their chat settings.
     *
     * @param player the player
     * @param message the message
     * @param position the position
     * @param uuid the UUID of the sender, if any
     * @return if the message was sent
     */
    fun sendMessage(player: Player, message: Component, position: ChatPosition, uuid: UUID?): Boolean {
        if (getChatMessageType(player).accepts(position)) {
            player.playerConnection.sendPacket(ChatMessagePacket(message, position, uuid!!))
            return true
        }
        return false
    }

    /**
     * Sends a message to some players, respecting their chat settings.
     *
     * @param players the players
     * @param message the message
     * @param position the position
     * @param uuid the UUID of the sender, if any
     */
    @JvmStatic
    fun sendMessage(
        players: Collection<Player?>, message: Component,
        position: ChatPosition, uuid: UUID?
    ) {
        PacketUtils.sendGroupedPacket(
            players, ChatMessagePacket(message, position, uuid!!)
        ) { player: Player -> getChatMessageType(player).accepts(position) }
    }

    /**
     * Checks if the server should receive messages from a player, given their chat settings.
     *
     * @param player the player
     * @return if the server should receive messages from them
     */
    fun canReceiveMessage(player: Player): Boolean {
        return getChatMessageType(player) == ChatMessageType.FULL
    }

    /**
     * Checks if the server should receive commands from a player, given their chat settings.
     *
     * @param player the player
     * @return if the server should receive commands from them
     */
    fun canReceiveCommand(player: Player): Boolean {
        return getChatMessageType(player) != ChatMessageType.NONE
    }

    /**
     * Sends a message to the player informing them we are rejecting their message or command.
     *
     * @param player the player
     */
    fun sendRejectionMessage(player: Player) {
        player.playerConnection.sendPacket(CANNOT_SEND_PACKET)
    }

    /**
     * Gets the chat message type for a player, returning [ChatMessageType.FULL] if not set.
     *
     * @param player the player
     * @return the chat message type
     */
    private fun getChatMessageType(player: Player): ChatMessageType {
        return Objects.requireNonNullElse(player.settings.chatMessageType, ChatMessageType.FULL)
    }
}