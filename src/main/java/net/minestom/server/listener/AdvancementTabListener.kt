package net.minestom.server.listener

import net.minestom.server.MinecraftServer.Companion.blockManager
import net.minestom.server.utils.chunk.ChunkUtils.isLoaded
import net.minestom.server.utils.validate.Check.stateCondition
import net.minestom.server.MinecraftServer.Companion.commandManager
import net.minestom.server.MinecraftServer.Companion.connectionManager
import net.minestom.server.network.packet.client.play.ClientPlayerAbilitiesPacket
import net.minestom.server.event.player.PlayerStartFlyingEvent
import net.minestom.server.event.player.PlayerStopFlyingEvent
import net.minestom.server.network.packet.client.play.ClientAdvancementTabPacket
import net.minestom.server.event.player.AdvancementTabEvent
import net.minestom.server.network.packet.client.play.ClientAnimationPacket
import net.minestom.server.entity.Player.Hand
import net.minestom.server.item.ItemStack
import net.minestom.server.event.player.PlayerHandAnimationEvent
import java.lang.Runnable
import net.minestom.server.instance.block.BlockManager
import net.minestom.server.MinecraftServer
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket
import net.minestom.server.inventory.PlayerInventory
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.utils.chunk.ChunkUtils
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.instance.block.BlockHandler
import net.minestom.server.instance.block.BlockHandler.Interaction
import net.minestom.server.listener.BlockPlacementListener
import net.minestom.server.item.Material
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent
import net.minestom.server.entity.GameMode
import net.minestom.server.network.packet.server.play.BlockChangePacket
import net.minestom.server.collision.CollisionUtils
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.instance.block.rule.BlockPlacementRule
import net.minestom.server.instance.block.BlockHandler.PlayerPlacement
import net.minestom.server.command.CommandManager
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket
import net.minestom.server.message.Messenger
import net.minestom.server.listener.ChatMessageListener
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.message.ChatPosition
import net.kyori.adventure.text.event.ClickEvent
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher

object AdvancementTabListener {
    fun listener(packet: ClientAdvancementTabPacket, player: Player?) {
        val tabIdentifier = packet.tabIdentifier()
        if (tabIdentifier != null) {
            EventDispatcher.call(AdvancementTabEvent(player!!, packet.action(), tabIdentifier))
        }
    }
}