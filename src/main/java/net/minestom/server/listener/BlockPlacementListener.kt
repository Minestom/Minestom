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
import net.minestom.server.instance.Chunk

object BlockPlacementListener {
    private val BLOCK_MANAGER = blockManager
    fun listener(packet: ClientPlayerBlockPlacementPacket, player: Player) {
        val playerInventory = player.inventory
        val hand = packet.hand()
        val blockFace = packet.blockFace()
        val blockPosition = packet.blockPosition()
        val instance = player.instance ?: return

        // Prevent outdated/modified client data
        val interactedChunk = instance.getChunkAt(blockPosition)
        if (!isLoaded(interactedChunk)) {
            // Client tried to place a block in an unloaded chunk, ignore the request
            return
        }
        val usedItem = player.getItemInHand(hand)
        val interactedBlock = instance.getBlock(blockPosition)

        // Interact at block
        // FIXME: onUseOnBlock
        val playerBlockInteractEvent = PlayerBlockInteractEvent(player, hand, interactedBlock, blockPosition, blockFace)
        EventDispatcher.call(playerBlockInteractEvent)
        var blockUse = playerBlockInteractEvent.isBlockingItemUse
        if (!playerBlockInteractEvent.isCancelled) {
            val handler = interactedBlock.handler()
            if (handler != null) {
                blockUse =
                    blockUse or !handler.onInteract(Interaction(interactedBlock, instance, blockPosition, player, hand))
            }
        }
        if (blockUse) {
            refresh(player, interactedChunk)
            return
        }
        val useMaterial = usedItem.material
        if (!useMaterial.isBlock) {
            // Player didn't try to place a block but interacted with one
            val event = PlayerUseItemOnBlockEvent(player, hand, usedItem, blockPosition, blockFace)
            EventDispatcher.call(event)
            return
        }

        // Verify if the player can place the block
        var canPlaceBlock = true
        // Check if the player is allowed to place blocks based on their game mode
        if (player.gameMode == GameMode.SPECTATOR) {
            canPlaceBlock = false // Spectators can't place blocks
        } else if (player.gameMode == GameMode.ADVENTURE) {
            //Check if the block can be placed on the block
            canPlaceBlock = usedItem.meta.canPlaceOn.contains(interactedBlock)
        }

        // Get the newly placed block position
        val offsetX = if (blockFace == BlockFace.WEST) -1 else if (blockFace == BlockFace.EAST) 1 else 0
        val offsetY = if (blockFace == BlockFace.BOTTOM) -1 else if (blockFace == BlockFace.TOP) 1 else 0
        val offsetZ = if (blockFace == BlockFace.NORTH) -1 else if (blockFace == BlockFace.SOUTH) 1 else 0
        val placementPosition = blockPosition.add(offsetX.toDouble(), offsetY.toDouble(), offsetZ.toDouble())
        if (!canPlaceBlock) {
            // Send a block change with the real block in the instance to keep the client in sync,
            // using refreshChunk results in the client not being in sync
            // after rapid invalid block placements
            val block = instance.getBlock(placementPosition)
            player.playerConnection.sendPacket(BlockChangePacket(placementPosition, block))
            return
        }
        val chunk = instance.getChunkAt(placementPosition)
        stateCondition(
            !isLoaded(chunk),
            "A player tried to place a block in the border of a loaded chunk {0}", placementPosition
        )
        if (chunk!!.isReadOnly) {
            refresh(player, chunk)
            return
        }
        val placedBlock = useMaterial.block()
        if (!CollisionUtils.canPlaceBlockAt(instance, placementPosition, placedBlock)) {
            refresh(player, chunk)
            return
        }
        // BlockPlaceEvent check
        val playerBlockPlaceEvent =
            PlayerBlockPlaceEvent(player, placedBlock, blockFace, placementPosition, packet.hand())
        playerBlockPlaceEvent.consumeBlock(player.gameMode != GameMode.CREATIVE)
        EventDispatcher.call(playerBlockPlaceEvent)
        if (playerBlockPlaceEvent.isCancelled) {
            refresh(player, chunk)
            return
        }

        // BlockPlacementRule check
        var resultBlock = playerBlockPlaceEvent.block
        val blockPlacementRule = BLOCK_MANAGER.getBlockPlacementRule(resultBlock)
        if (blockPlacementRule != null) {
            // Get id from block placement rule instead of the event
            resultBlock = blockPlacementRule.blockPlace(instance, resultBlock, blockFace, blockPosition, player)
        }
        if (resultBlock == null) {
            refresh(player, chunk)
            return
        }
        // Place the block
        instance.placeBlock(
            PlayerPlacement(
                resultBlock, instance, placementPosition, player, hand, blockFace,
                packet.cursorPositionX(), packet.cursorPositionY(), packet.cursorPositionZ()
            )
        )
        // Block consuming
        if (playerBlockPlaceEvent.doesConsumeBlock()) {
            // Consume the block in the player's hand
            val newUsedItem = usedItem.stackingRule.apply(usedItem, usedItem.amount - 1)
            playerInventory.setItemInHand(hand, newUsedItem)
        }
    }

    private fun refresh(player: Player, chunk: Chunk?) {
        player.inventory.update()
        chunk!!.sendChunk(player)
    }
}