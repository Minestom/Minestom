package net.minestom.server.listener

import net.kyori.adventure.text.Component
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

object ChatMessageListener {
    private val COMMAND_MANAGER = commandManager
    private val CONNECTION_MANAGER = connectionManager
    fun listener(packet: ClientChatMessagePacket, player: Player) {
        val message = packet.message()
        val cmdPrefix = CommandManager.COMMAND_PREFIX
        if (message.startsWith(cmdPrefix)) {
            // The message is a command
            val command = message.replaceFirst(cmdPrefix.toRegex(), "")

            // check if we can receive commands
            if (Messenger.canReceiveCommand(player)) {
                COMMAND_MANAGER.execute(player, command)
            } else {
                Messenger.sendRejectionMessage(player)
            }

            // Do not call chat event
            return
        }

        // check if we can receive messages
        if (!Messenger.canReceiveMessage(player)) {
            Messenger.sendRejectionMessage(player)
            return
        }
        val players = CONNECTION_MANAGER.onlinePlayers
        val playerChatEvent = PlayerChatEvent(player, players, { buildDefaultChatMessage(player, message) }, message)

        // Call the event
        EventDispatcher.callCancellable(playerChatEvent) {
            val formatFunction = playerChatEvent.chatFormatFunction
            val textObject: Component
            textObject = // Custom format
                formatFunction?.apply(playerChatEvent) ?: // Default format
                        playerChatEvent.defaultChatFormat.get()
            val recipients = playerChatEvent.recipients
            if (!recipients.isEmpty()) {
                // delegate to the messenger to avoid sending messages we shouldn't be
                Messenger.sendMessage(recipients, textObject, ChatPosition.CHAT, player.getUuid())
            }
        }
    }

    private fun buildDefaultChatMessage(player: Player, message: String): Component {
        val username = player.username
        return Component.translatable("chat.type.text")
            .args(
                Component.text(username)
                    .insertion(username)
                    .clickEvent(ClickEvent.suggestCommand("/msg $username "))
                    .hoverEvent(player),
                Component.text(message)
            )
    }
}