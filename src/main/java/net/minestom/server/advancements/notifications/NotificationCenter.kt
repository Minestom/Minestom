package net.minestom.server.advancements.notifications

import net.kyori.adventure.text.Component
import net.minestom.server.advancements.FrameType
import net.minestom.server.item.Material
import net.minestom.server.item.ItemStack
import net.minestom.server.network.packet.server.play.AdvancementsPacket
import net.minestom.server.network.packet.server.play.AdvancementsPacket.AdvancementMapping
import net.minestom.server.advancements.notifications.NotificationCenter
import net.minestom.server.network.packet.server.play.AdvancementsPacket.ProgressMapping
import net.minestom.server.network.packet.server.play.AdvancementsPacket.DisplayData
import net.minestom.server.network.packet.server.play.AdvancementsPacket.Criteria
import net.minestom.server.network.packet.server.play.AdvancementsPacket.CriterionProgress
import net.minestom.server.network.packet.server.play.AdvancementsPacket.AdvancementProgress
import net.minestom.server.advancements.AdvancementTab
import net.minestom.server.utils.PacketUtils
import net.minestom.server.advancements.AdvancementRoot
import net.minestom.server.Viewable
import net.minestom.server.entity.Player
import net.minestom.server.network.player.PlayerConnection
import java.util.List
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

/**
 * Used to send one or multiples [Notification].
 *
 *
 * Works by sending a completed advancement and remove it immediately.
 *
 *
 * You can simply create a [Notification] object and call [.send].
 */
object NotificationCenter {
    private const val IDENTIFIER = "minestom:notification"
    private val REMOVE_PACKET = AdvancementsPacket(false, List.of(), List.of(IDENTIFIER), List.of())

    /**
     * Send a [Notification] to one player.
     *
     * @param notification the [Notification] to send
     * @param player       the player to send the notification to
     */
    fun send(notification: Notification, player: Player) {
        player.sendPacket(createPacket(notification))
        player.sendPacket(REMOVE_PACKET)
    }

    /**
     * Send a [Notification] to a collection of players.
     *
     * @param notification the [Notification] to send
     * @param players      the collection of players to send the notification to
     */
    fun send(notification: Notification, players: Collection<Player>) {
        // Can't use PacketWriterUtils because we need the packets to come in the correct order
        players.forEach(Consumer { player: Player -> send(notification, player) })
    }

    /**
     * Create the [AdvancementsPacket] responsible for showing the Toast to players
     *
     * @param notification the notification
     * @return the packet used to show the Toast
     */
    private fun createPacket(notification: Notification): AdvancementsPacket {
        // For An advancement to be shown, it must have all of its criteria achieved (progress 100%)
        // Create a Criteria that we can set to 100% achieved.
        val displayData = DisplayData(
            notification.title(), Component.text("Articdive was here. #Minestom"),
            notification.icon(), notification.frameType(),
            0x6, null, 0f, 0f
        )
        val criteria = Criteria(
            "minestom:some_criteria",
            CriterionProgress(System.currentTimeMillis())
        )
        val advancement = AdvancementsPacket.Advancement(
            null, displayData,
            List.of(criteria.criterionIdentifier()),
            List.of(AdvancementsPacket.Requirement(List.of(criteria.criterionIdentifier())))
        )
        val mapping = AdvancementMapping(IDENTIFIER, advancement)
        val progressMapping = ProgressMapping(
            IDENTIFIER,
            AdvancementProgress(List.of(criteria))
        )
        return AdvancementsPacket(false, List.of(mapping), List.of(), List.of(progressMapping))
    }
}