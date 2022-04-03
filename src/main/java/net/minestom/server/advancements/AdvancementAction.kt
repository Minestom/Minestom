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
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ConcurrentHashMap

enum class AdvancementAction {
    OPENED_TAB, CLOSED_SCREEN
}