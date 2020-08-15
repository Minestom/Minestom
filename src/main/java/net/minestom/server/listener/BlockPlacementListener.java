package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.chunk.ChunkUtils;

import java.util.Set;

public class BlockPlacementListener {

    public static void listener(ClientPlayerBlockPlacementPacket packet, Player player) {
        final PlayerInventory playerInventory = player.getInventory();
        final Player.Hand hand = packet.hand;
        final BlockFace blockFace = packet.blockFace;
        final BlockPosition blockPosition = packet.blockPosition;
        final Direction direction = blockFace.toDirection();

        final Instance instance = player.getInstance();
        if (instance == null)
            return;

        final ItemStack usedItem = player.getItemInHand(hand);

        // Interact at block
        final boolean cancel = usedItem.onUseOnBlock(player, hand, blockPosition, direction);
        PlayerBlockInteractEvent playerBlockInteractEvent = new PlayerBlockInteractEvent(player, blockPosition, hand, blockFace);
        playerBlockInteractEvent.setCancelled(cancel);
        playerBlockInteractEvent.setBlockingItemUse(cancel);
        player.callCancellableEvent(PlayerBlockInteractEvent.class, playerBlockInteractEvent, () -> {
            final CustomBlock customBlock = instance.getCustomBlock(blockPosition);
            if (customBlock != null) {
                final Data data = instance.getBlockData(blockPosition);
                final boolean blocksItem = customBlock.onInteract(player, hand, blockPosition, data);
                if (blocksItem) {
                    playerBlockInteractEvent.setBlockingItemUse(true);
                }
            }
        });

        if (playerBlockInteractEvent.isBlockingItemUse()) {
            return;
        }

        // Check if item at hand is a block
        final Material material = usedItem.getMaterial();
        if (material == Material.AIR) {
            return;
        }

        // Get the newly placed block position
        final int offsetX = blockFace == BlockFace.WEST ? -1 : blockFace == BlockFace.EAST ? 1 : 0;
        final int offsetY = blockFace == BlockFace.BOTTOM ? -1 : blockFace == BlockFace.TOP ? 1 : 0;
        final int offsetZ = blockFace == BlockFace.NORTH ? -1 : blockFace == BlockFace.SOUTH ? 1 : 0;

        blockPosition.add(offsetX, offsetY, offsetZ);


        final Chunk chunk = instance.getChunkAt(blockPosition);
        // The concerned chunk will be send to the player if an error occur
        // This will ensure that the player has the correct version of the chunk
        boolean refreshChunk = false;

        if (material.isBlock()) {
            final Block block = material.getBlock();
            final Set<Entity> entities = instance.getChunkEntities(chunk);
            // Check if the player is trying to place a block in an entity
            boolean intersect = false;
            if (block.isSolid()) {
                for (Entity entity : entities) {
                    intersect = entity.getBoundingBox().intersect(blockPosition);
                    if (intersect)
                        break;
                }
            }

            if (!intersect) {
                // BlockPlacementRule check
                final BlockManager blockManager = MinecraftServer.getBlockManager();
                final BlockPlacementRule blockPlacementRule = blockManager.getBlockPlacementRule(block);
                final short blockStateId = blockPlacementRule == null ? block.getBlockId() :
                        blockPlacementRule.blockPlace(instance, block, blockFace, player);

                PlayerBlockPlaceEvent playerBlockPlaceEvent = new PlayerBlockPlaceEvent(player, blockStateId, (short) 0, blockPosition, packet.hand);
                playerBlockPlaceEvent.consumeBlock(player.getGameMode() != GameMode.CREATIVE);

                // BlockPlacementRule check
                final boolean canPlace = blockPlacementRule == null || blockPlacementRule.canPlace(instance, blockPosition);

                player.callEvent(PlayerBlockPlaceEvent.class, playerBlockPlaceEvent);
                if (!playerBlockPlaceEvent.isCancelled() && canPlace) {
                    final short customBlockId = playerBlockPlaceEvent.getCustomBlockId();
                    if (customBlockId != 0) {
                        instance.setSeparateBlocks(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), playerBlockPlaceEvent.getBlockStateId(), playerBlockPlaceEvent.getCustomBlockId());
                    } else {
                        instance.setBlockStateId(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), playerBlockPlaceEvent.getBlockStateId());
                    }
                    if (playerBlockPlaceEvent.doesConsumeBlock()) {
                        // Consume the block in the player's hand
                        final ItemStack newUsedItem = usedItem.consume(1);

                        if (newUsedItem != null) {
                            playerInventory.setItemInHand(hand, newUsedItem);
                        }
                    }
                } else {
                    refreshChunk = true;
                }
            } else {
                refreshChunk = true;
            }
        } else {
            // Player didn't try to place a block but interacted with one
            PlayerUseItemOnBlockEvent event = new PlayerUseItemOnBlockEvent(player, hand, usedItem, blockPosition, direction);
            player.callEvent(PlayerUseItemOnBlockEvent.class, event);
            refreshChunk = true;
        }

        // Refresh chunk section if needed
        if (refreshChunk) {
            chunk.sendChunkSectionUpdate(ChunkUtils.getSectionAt(blockPosition.getY()), player);
        }

        player.getInventory().refreshSlot(player.getHeldSlot());
    }

}
