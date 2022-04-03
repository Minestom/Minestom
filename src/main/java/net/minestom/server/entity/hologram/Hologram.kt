package net.minestom.server.entity.hologram

import net.kyori.adventure.text.Component
import net.minestom.server.entity.metadata.EntityMeta.setNotifyAboutChanges
import net.minestom.server.entity.metadata.other.ArmorStandMeta.setMarker
import net.minestom.server.entity.metadata.other.ArmorStandMeta.setSmall
import net.minestom.server.entity.metadata.EntityMeta.isHasNoGravity
import net.minestom.server.entity.metadata.EntityMeta.customName
import net.minestom.server.entity.metadata.EntityMeta.isCustomNameVisible
import net.minestom.server.entity.metadata.EntityMeta.isInvisible
import net.minestom.server.utils.validate.Check.stateCondition
import net.minestom.server.Viewable
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.metadata.other.ArmorStandMeta
import net.minestom.server.entity.hologram.Hologram
import net.minestom.server.instance.Instance

/**
 * Represents an invisible armor stand showing a [Component].
 */
class Hologram @JvmOverloads constructor(
    instance: Instance?,
    spawnPosition: Pos,
    text: Component?,
    autoViewable: Boolean = true,
    marker: Boolean = false
) : Viewable {
    /**
     * Gets the hologram entity (armor stand).
     *
     * @return the hologram entity
     */
    val entity: Entity
    private var yOffset = 0f
    private var position: Pos
    /**
     * Gets the hologram text.
     *
     * @return the hologram text
     */
    /**
     * Changes the hologram text.
     *
     * @param text the new hologram text
     */
    var text: Component? = null
        set(text) {
            checkRemoved()
            field = text
            entity.customName = text
        }

    /**
     * Checks if the hologram is still present.
     *
     * @return true if the hologram is present, false otherwise
     */
    var isRemoved = false
        private set
    /**
     * Constructs a new [Hologram] with the given parameters.
     *
     * @param instance      The instance where the hologram should be spawned.
     * @param spawnPosition The spawn position of this hologram.
     * @param text          The text of this hologram.
     * @param autoViewable  `true`if the hologram should be visible automatically, otherwise `false`.
     */
    /**
     * Constructs a new [Hologram] with the given parameters.
     *
     * @param instance      The instance where the hologram should be spawned.
     * @param spawnPosition The spawn position of this hologram.
     * @param text          The text of this hologram.
     * @param autoViewable  `true`if the hologram should be visible automatically, otherwise `false`.
     */
    /**
     * Constructs a new [Hologram] with the given parameters.
     *
     * @param instance      The instance where the hologram should be spawned.
     * @param spawnPosition The spawn position of this hologram.
     * @param text          The text of this hologram.
     */
    init {
        entity = Entity(EntityType.ARMOR_STAND)
        val armorStandMeta = entity.entityMeta as ArmorStandMeta
        armorStandMeta.setNotifyAboutChanges(false)
        if (marker) {
            yOffset = MARKER_OFFSET_Y
            armorStandMeta.setMarker(true)
        } else {
            yOffset = OFFSET_Y
            armorStandMeta.setSmall(true)
        }
        armorStandMeta.isHasNoGravity = true
        armorStandMeta.customName = Component.empty()
        armorStandMeta.isCustomNameVisible = true
        armorStandMeta.isInvisible = true
        armorStandMeta.setNotifyAboutChanges(true)
        entity.setInstance(instance!!, spawnPosition.add(0.0, yOffset.toDouble(), 0.0))
        entity.isAutoViewable = autoViewable
        position = spawnPosition
        text = text
    }

    /**
     * Gets the position of the hologram.
     *
     * @return the hologram's position
     */
    fun getPosition(): Pos {
        return position
    }

    /**
     * Changes the position of the hologram.
     *
     * @param position the new hologram's position
     */
    fun setPosition(position: Pos) {
        checkRemoved()
        this.position = position.add(0.0, yOffset.toDouble(), 0.0)
        entity.teleport(this.position)
    }

    /**
     * Removes the hologram.
     */
    fun remove() {
        isRemoved = true
        entity.remove()
    }

    /**
     * {@inheritDoc}
     */
    override fun addViewer(player: Player): Boolean {
        return entity.addViewer(player)
    }

    /**
     * {@inheritDoc}
     */
    override fun removeViewer(player: Player): Boolean {
        return entity.removeViewer(player)
    }

    /**
     * {@inheritDoc}
     */
    override fun getViewers(): Set<Player> {
        return entity.viewers
    }

    /**
     * @see .isRemoved
     */
    private fun checkRemoved() {
        stateCondition(isRemoved, "You cannot interact with a removed Hologram")
    }

    companion object {
        private const val OFFSET_Y = -0.9875f
        private const val MARKER_OFFSET_Y = -0.40625f
    }
}