package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.server.network.packet.server.play.AcknowledgeBlockChangePacket;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.validate.Check;

public class BlockPlacementListener {
    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    public static void listener(ClientPlayerBlockPlacementPacket packet, Player player) {
        final PlayerInventory playerInventory = player.getInventory();
        final Player.Hand hand = packet.hand();
        final BlockFace blockFace = packet.blockFace();
        final Point blockPosition = packet.blockPosition();

        final Instance instance = player.getInstance();
        if (instance == null)
            return;

        // Prevent outdated/modified client data
        final Chunk interactedChunk = instance.getChunkAt(blockPosition);
        if (!ChunkUtils.isLoaded(interactedChunk)) {
            // Client tried to place a block in an unloaded chunk, ignore the request
            return;
        }

        final ItemStack usedItem = player.getItemInHand(hand);
        final Block interactedBlock = instance.getBlock(blockPosition);

        final Point cursorPosition = new Vec(packet.cursorPositionX(), packet.cursorPositionY(), packet.cursorPositionZ());

        // Interact at block
        // FIXME: onUseOnBlock
        PlayerBlockInteractEvent playerBlockInteractEvent = new PlayerBlockInteractEvent(player, hand, interactedBlock, blockPosition, cursorPosition, blockFace);
        EventDispatcher.call(playerBlockInteractEvent);
        boolean blockUse = playerBlockInteractEvent.isBlockingItemUse();
        if (!playerBlockInteractEvent.isCancelled()) {
            final var handler = interactedBlock.handler();
            if (handler != null) {
                blockUse |= !handler.onInteract(new BlockHandler.Interaction(interactedBlock, instance, blockPosition, cursorPosition, player, hand));
            }
        }
        if (blockUse) {
            refresh(player, interactedChunk);
            return;
        }

        final Material useMaterial = usedItem.material();
        if (!useMaterial.isBlock()) {
            // Player didn't try to place a block but interacted with one
            PlayerUseItemOnBlockEvent event = new PlayerUseItemOnBlockEvent(player, hand, usedItem, blockPosition, cursorPosition, blockFace);
            EventDispatcher.call(event);
            return;
        }

        // Verify if the player can place the block
        boolean canPlaceBlock = true;
        // Check if the player is allowed to place blocks based on their game mode
        if (player.getGameMode() == GameMode.SPECTATOR) {
            canPlaceBlock = false; // Spectators can't place blocks
        } else if (player.getGameMode() == GameMode.ADVENTURE) {
            //Check if the block can be placed on the block
            canPlaceBlock = usedItem.meta().canPlaceOn(interactedBlock);
        }

        // Get the newly placed block position
        final int offsetX = blockFace == BlockFace.WEST ? -1 : blockFace == BlockFace.EAST ? 1 : 0;
        final int offsetY = blockFace == BlockFace.BOTTOM ? -1 : blockFace == BlockFace.TOP ? 1 : 0;
        final int offsetZ = blockFace == BlockFace.NORTH ? -1 : blockFace == BlockFace.SOUTH ? 1 : 0;
        final Point placementPosition = blockPosition.add(offsetX, offsetY, offsetZ);

        if (!canPlaceBlock) {
            // Send a block change with the real block in the instance to keep the client in sync,
            // using refreshChunk results in the client not being in sync
            // after rapid invalid block placements
            final Block block = instance.getBlock(placementPosition);
            player.sendPacket(new BlockChangePacket(placementPosition, block));
            return;
        }

        final Chunk chunk = instance.getChunkAt(placementPosition);
        Check.stateCondition(!ChunkUtils.isLoaded(chunk),
                "A player tried to place a block in the border of a loaded chunk {0}", placementPosition);
        if (chunk.isReadOnly()) {
            refresh(player, chunk);
            return;
        }

        final Block placedBlock = useMaterial.block();
        Entity collisionEntity = CollisionUtils.canPlaceBlockAt(instance, placementPosition, placedBlock);
        if (collisionEntity != null) {
            // If a player is trying to place a block on themselves, the client will send a block change but will not set the block on the client
            // For this reason, the block doesn't need to be updated for the client

            // Client also doesn't predict placement of blocks on entities, but we need to refresh for cases where bounding boxes on the server don't match the client
            if (collisionEntity != player)
                refresh(player, chunk);
            
            return;
        }

        // BlockPlaceEvent check
        PlayerBlockPlaceEvent playerBlockPlaceEvent = new PlayerBlockPlaceEvent(player, placedBlock, blockFace, placementPosition, packet.hand());
        playerBlockPlaceEvent.consumeBlock(player.getGameMode() != GameMode.CREATIVE);
        EventDispatcher.call(playerBlockPlaceEvent);
        if (playerBlockPlaceEvent.isCancelled()) {
            refresh(player, chunk);
            return;
        }

        // BlockPlacementRule check
        Block resultBlock = playerBlockPlaceEvent.getBlock();
        final BlockPlacementRule blockPlacementRule = BLOCK_MANAGER.getBlockPlacementRule(resultBlock);
        if (blockPlacementRule != null) {
            // Get id from block placement rule instead of the event
            resultBlock = blockPlacementRule.blockPlace(instance, resultBlock, blockFace, blockPosition, player);
        }
        if (resultBlock == null) {
            refresh(player, chunk);
            return;
        }
        // Place the block
        player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
        instance.placeBlock(new BlockHandler.PlayerPlacement(resultBlock, instance, placementPosition, player, hand, blockFace,
                packet.cursorPositionX(), packet.cursorPositionY(), packet.cursorPositionZ()));
        // Block consuming
        if (playerBlockPlaceEvent.doesConsumeBlock()) {
            // Consume the block in the player's hand
            final ItemStack newUsedItem = usedItem.consume(1);
            playerInventory.setItemInHand(hand, newUsedItem);
        } else {
            // Prevent invisible item on client
            playerInventory.update();   
        }
    }

    private static void refresh(Player player, Chunk chunk) {
        player.getInventory().update();
        chunk.sendChunk(player);
    }
}
