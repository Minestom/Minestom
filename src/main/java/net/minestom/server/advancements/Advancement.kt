package net.minestom.server.advancements

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

/**
 * Represents an advancement located in an [AdvancementTab].
 *
 *
 * All fields are dynamic, changing one will update the advancement in the specific [AdvancementTab].
 */
open class Advancement(
    private var title: Component, private var description: Component,
    private var icon: ItemStack, private var frameType: FrameType,
    private var x: Float, private var y: Float
) {
    /**
     * Gets the advancement tab linked to this advancement.
     *
     * @return the [AdvancementTab] linked to this advancement, null if not linked to anything yet
     */
    var tab: AdvancementTab? = null
        protected set

    /**
     * Gets if the advancement is achieved.
     *
     * @return true if the advancement is achieved
     */
    var isAchieved = false
        private set
    private var background // Only on root
            : String? = null
    private var toast = false
    var isHidden = false
        private set
    /**
     * Gets the identifier of this advancement, used to register the advancement, use it as a parent and to retrieve it later
     * in the [AdvancementTab].
     *
     * @return the advancement identifier
     */
    /**
     * Changes the advancement identifier.
     *
     *
     * WARNING: unsafe, only used by [AdvancementTab] to initialize the advancement.
     *
     * @param identifier the new advancement identifier
     */
    protected var identifier: String? = null
        set

    /**
     * Gets the advancement parent.
     *
     * @return the advancement parent, null for [AdvancementRoot]
     */
    protected var parent: Advancement? = null
        set

    // Packet
    private var criteria: Criteria? = null

    constructor(
        title: Component, description: Component,
        icon: Material, frameType: FrameType,
        x: Float, y: Float
    ) : this(title, description, ItemStack.of(icon), frameType, x, y) {
    }

    /**
     * Makes the advancement achieved.
     *
     * @param achieved true to make it achieved
     * @return this advancement
     */
    fun setAchieved(achieved: Boolean): Advancement {
        isAchieved = achieved
        update()
        return this
    }

    fun setTab(tab: AdvancementTab) {
        this.tab = tab
    }

    /**
     * Gets the title of the advancement.
     *
     * @return the title
     */
    fun getTitle(): Component {
        return title
    }

    /**
     * Changes the advancement title.
     *
     * @param title the new title
     */
    fun setTitle(title: Component) {
        this.title = title
        update()
    }

    /**
     * Gets the description of the advancement.
     *
     * @return the description title
     */
    fun getDescription(): Component {
        return description
    }

    /**
     * Changes the description title.
     *
     * @param description the new description
     */
    fun setDescription(description: Component) {
        this.description = description
        update()
    }

    /**
     * Gets the advancement icon.
     *
     * @return the advancement icon
     */
    fun getIcon(): ItemStack {
        return icon
    }

    /**
     * Changes the advancement icon.
     *
     * @param icon the new advancement icon
     */
    fun setIcon(icon: ItemStack) {
        this.icon = icon
        update()
    }

    /**
     * Gets if this advancement has a toast.
     *
     * @return true if the advancement has a toast
     */
    fun hasToast(): Boolean {
        return toast
    }

    /**
     * Makes this argument a toast.
     *
     * @param toast true to make this advancement a toast
     * @return this advancement
     */
    fun showToast(toast: Boolean): Advancement {
        this.toast = toast
        return this
    }

    fun setHidden(hidden: Boolean): Advancement {
        isHidden = hidden
        update()
        return this
    }

    /**
     * Gets the advancement frame type.
     *
     * @return this advancement frame type
     */
    fun getFrameType(): FrameType {
        return frameType
    }

    /**
     * Changes the advancement frame type.
     *
     * @param frameType the new frame type
     */
    fun setFrameType(frameType: FrameType) {
        this.frameType = frameType
        update()
    }

    /**
     * Gets the X position of this advancement.
     *
     * @return this advancement X
     */
    fun getX(): Float {
        return x
    }

    /**
     * Changes this advancement X coordinate.
     *
     * @param x the new X coordinate
     */
    fun setX(x: Float) {
        this.x = x
        update()
    }

    /**
     * Gets the Y position of this advancement.
     *
     * @return this advancement Y
     */
    fun getY(): Float {
        return y
    }

    /**
     * Changes this advancement Y coordinate.
     *
     * @param y the new Y coordinate
     */
    fun setY(y: Float) {
        this.y = y
        update()
    }

    /**
     * Sets the background.
     *
     *
     * Only available for [AdvancementRoot].
     *
     * @param background the new background
     */
    protected fun setBackground(background: String?) {
        this.background = background
    }

    fun toProgressMapping(): ProgressMapping {
        val advancementProgress = AdvancementProgress(List.of(criteria))
        return ProgressMapping(identifier!!, advancementProgress)
    }

    protected fun toDisplayData(): DisplayData {
        return DisplayData(
            title, description, icon,
            frameType, flags, background, x, y
        )
    }

    /**
     * Converts this advancement to an [AdvancementsPacket.AdvancementMapping].
     *
     * @return the mapping of this advancement
     */
    fun toMapping(): AdvancementMapping {
        val parent = parent
        val parentIdentifier = parent?.identifier
        val adv = AdvancementsPacket.Advancement(
            parentIdentifier, toDisplayData(),
            List.of(criteria!!.criterionIdentifier()),
            List.of(
                AdvancementsPacket.Requirement(
                    List.of(
                        criteria!!.criterionIdentifier()
                    )
                )
            )
        )
        return AdvancementMapping(identifier!!, adv)
    }

    /**
     * Gets the packet used to add this advancement to the already existing tab.
     *
     * @return the packet to add this advancement
     */
    val updatePacket: AdvancementsPacket
        get() = AdvancementsPacket(
            false, List.of(toMapping()),
            List.of(), List.of(toProgressMapping())
        )

    /**
     * Sends update to all tab viewers if one of the advancement value changes.
     */
    protected fun update() {
        updateCriteria()
        if (tab != null) {
            val viewers: Set<Player?> = tab!!.viewers
            val createPacket = tab!!.createPacket()
            PacketUtils.sendGroupedPacket(viewers, tab!!.removePacket)
            PacketUtils.sendGroupedPacket(viewers, createPacket)
        }
    }

    fun updateCriteria() {
        val achievedDate = if (isAchieved) System.currentTimeMillis() else null
        val progress = CriterionProgress(achievedDate)
        criteria = Criteria(identifier!!, progress)
    }

    private val flags: Int
        private get() {
            var result: Byte = 0
            if (background != null) result = result or 0x1
            if (hasToast()) result = result or 0x2
            if (isHidden) result = result or 0x4
            return result.toInt()
        }
}