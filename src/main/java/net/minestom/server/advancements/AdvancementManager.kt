package net.minestom.server.advancements

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
import net.minestom.server.network.player.PlayerConnection
import net.minestom.server.utils.validate.Check
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ConcurrentHashMap

/**
 * Used to manage all the registered [AdvancementTab].
 *
 *
 * Use [.createTab] to create a tab with the appropriate [AdvancementRoot].
 */
class AdvancementManager {
    // root identifier = its advancement tab
    private val advancementTabMap: MutableMap<String, AdvancementTab> = ConcurrentHashMap()

    /**
     * Creates a new [AdvancementTab] with a single [AdvancementRoot].
     *
     * @param rootIdentifier the root identifier
     * @param root           the root advancement
     * @return the newly created [AdvancementTab]
     * @throws IllegalStateException if a tab with the identifier `rootIdentifier` already exists
     */
    fun createTab(rootIdentifier: String, root: AdvancementRoot): AdvancementTab {
        Check.stateCondition(
            advancementTabMap.containsKey(rootIdentifier),
            "A tab with the identifier '$rootIdentifier' already exists"
        )
        val advancementTab = AdvancementTab(rootIdentifier, root)
        advancementTabMap[rootIdentifier] = advancementTab
        return advancementTab
    }

    /**
     * Gets an advancement tab by its root identifier.
     *
     * @param rootIdentifier the root identifier of the tab
     * @return the [AdvancementTab] associated with the identifier, null if not any
     */
    fun getTab(rootIdentifier: String): AdvancementTab? {
        return advancementTabMap[rootIdentifier]
    }

    /**
     * Gets all the created [AdvancementTab].
     *
     * @return the collection containing all created [AdvancementTab]
     */
    val tabs: Collection<AdvancementTab>
        get() = advancementTabMap.values
}