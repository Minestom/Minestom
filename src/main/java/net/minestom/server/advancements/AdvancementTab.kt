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
import net.minestom.server.entity.Player
import net.minestom.server.network.player.PlayerConnection
import net.minestom.server.utils.validate.Check
import java.util.*
import java.util.List
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a tab which can be shared between multiple players. Created using [AdvancementManager.createTab].
 *
 *
 * Each tab requires a root advancement and all succeeding advancements need to have a parent in the tab.
 * You can create a new advancement using [.createAdvancement].
 *
 *
 * Be sure to use [.addViewer] and [.removeViewer] to control which players can see the tab.
 * (all viewers will see the same tab, with the same amount of validated advancements etc... so shared).
 */
class AdvancementTab(
    rootIdentifier: String,
    /**
     * Gets the root advancement of this tab.
     *
     * @return the root advancement
     */
    val root: AdvancementRoot
) : Viewable {
    private val viewers: MutableSet<Player> = HashSet()

    // Advancement -> its parent
    private val advancementMap: MutableMap<Advancement, Advancement?> = HashMap()

    // the packet used to clear the tab (used to remove it and to update an advancement)
    // will never change (since the root identifier is always the same)
    val removePacket: AdvancementsPacket

    init {
        cacheAdvancement(rootIdentifier, root, null)
        removePacket = AdvancementsPacket(false, List.of(), List.of(rootIdentifier), List.of())
    }

    /**
     * Creates and add an advancement into this tab.
     *
     * @param identifier  the unique identifier
     * @param advancement the advancement to add
     * @param parent      the parent of this advancement, it cannot be null
     */
    fun createAdvancement(identifier: String, advancement: Advancement, parent: Advancement) {
        Check.stateCondition(
            !advancementMap.containsKey(parent),
            "You tried to set a parent which doesn't exist or isn't registered"
        )
        cacheAdvancement(identifier, advancement, parent)
        if (!getViewers().isEmpty()) {
            sendPacketToViewers(advancement.updatePacket)
        }
    }

    /**
     * Builds the packet which build the whole advancement tab.
     *
     * @return the packet adding this advancement tab and all its advancements
     */
    fun createPacket(): AdvancementsPacket {
        val mappings: MutableList<AdvancementMapping> = ArrayList()
        val progressMappings: MutableList<ProgressMapping> = ArrayList()
        for (advancement in advancementMap.keys) {
            mappings.add(advancement.toMapping())
            progressMappings.add(advancement.toProgressMapping())
        }
        return AdvancementsPacket(false, mappings, List.of(), progressMappings)
    }

    /**
     * Caches an advancement.
     *
     * @param identifier  the identifier of the advancement
     * @param advancement the advancement
     * @param parent      the parent of this advancement, only null for the root advancement
     */
    private fun cacheAdvancement(identifier: String, advancement: Advancement, parent: Advancement?) {
        Check.stateCondition(
            advancement.getTab() != null,
            "You tried to add an advancement already linked to a tab"
        )
        advancement.setTab(this)
        advancement.identifier = identifier
        advancement.parent = parent
        advancement.updateCriteria()
        advancementMap[advancement] = parent
    }

    @Synchronized
    override fun addViewer(player: Player): Boolean {
        val result = viewers.add(player)
        if (!result) {
            return false
        }
        val playerConnection = player.playerConnection

        // Send the tab to the player
        playerConnection.sendPacket(createPacket())
        addPlayer(player)
        return true
    }

    @Synchronized
    override fun removeViewer(player: Player): Boolean {
        if (!isViewer(player)) {
            return false
        }
        val playerConnection = player.playerConnection

        // Remove the tab
        if (!player.isRemoved) {
            playerConnection.sendPacket(removePacket)
        }
        removePlayer(player)
        return viewers.remove(player)
    }

    override fun getViewers(): Set<Player> {
        return viewers
    }

    /**
     * Adds the tab to the player set.
     *
     * @param player the player
     */
    private fun addPlayer(player: Player) {
        val tabs = PLAYER_TAB_MAP.computeIfAbsent(player.uuid) { p: UUID? -> CopyOnWriteArraySet() }
        tabs!!.add(this)
    }

    /**
     * Removes the tab from the player set.
     *
     * @param player the player
     */
    private fun removePlayer(player: Player) {
        val uuid = player.uuid
        if (!PLAYER_TAB_MAP.containsKey(uuid)) {
            return
        }
        val tabs = PLAYER_TAB_MAP[uuid]
        tabs!!.remove(this)
        if (tabs.isEmpty()) {
            PLAYER_TAB_MAP.remove(uuid)
        }
    }

    companion object {
        private val PLAYER_TAB_MAP: MutableMap<UUID, MutableSet<AdvancementTab?>?> = HashMap()

        /**
         * Gets all the tabs of a viewer.
         *
         * @param player the player to get the tabs from
         * @return all the advancement tabs that the player sees, can be null
         * if the player doesn't see anything
         */
        @JvmStatic
        fun getTabs(player: Player): Set<AdvancementTab?>? {
            return PLAYER_TAB_MAP.getOrDefault(player.uuid, null)
        }
    }
}