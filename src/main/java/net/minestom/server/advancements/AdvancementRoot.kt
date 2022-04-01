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
import net.minestom.server.network.player.PlayerConnection
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents an [Advancement] which is the root of an [AdvancementTab].
 * Every tab requires one since advancements needs to be linked to a parent.
 *
 *
 * The difference between this and an [Advancement] is that the root is responsible for the tab background.
 */
class AdvancementRoot : Advancement {
    constructor(
        title: Component, description: Component,
        icon: ItemStack, frameType: FrameType,
        x: Float, y: Float,
        background: String?
    ) : super(title, description, icon, frameType, x, y) {
        setBackground(background)
    }

    constructor(
        title: Component, description: Component,
        icon: Material, frameType: FrameType,
        x: Float, y: Float,
        background: String?
    ) : super(title, description, icon, frameType, x, y) {
        setBackground(background)
    }
}