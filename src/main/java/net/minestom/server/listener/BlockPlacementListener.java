package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
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
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.validate.Check;

import java.util.Set;

public class BlockPlacementListener {

    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    public static void listener(ClientPlayerBlockPlacementPacket packet, Player player) {
        final PlayerInventory playerInventory = player.getInventory();
        final Player.Hand hand = packet.hand;
        final BlockFace blockFace = packet.blockFace;
        final BlockPosition blockPosition = packet.blockPosition;
        final Direction direction = blockFace.toDirection();

        final Instance instance = player.getInstance();
        if (instance == null)
            return;

        // Prevent outdated/modified client data
        if (!ChunkUtils.isLoaded(instance.getChunkAt(blockPosition))) {
            // Client tried to place a block in an unloaded chunk, ignore the request
            return;
        }

        final ItemStack usedItem = player.getItemInHand(hand);
        final Block interactedBlock = instance.getBlock(blockPosition);

        // Interact at block
        // FIXME: onUseOnBlock
        PlayerBlockInteractEvent playerBlockInteractEvent = new PlayerBlockInteractEvent(player, hand, interactedBlock, blockPosition, blockFace);
        EventDispatcher.call(playerBlockInteractEvent);
        if (playerBlockInteractEvent.isBlockingItemUse()) {
            return;
        }

        final Material useMaterial = usedItem.getMaterial();

        // Verify if the player can place the block
        boolean canPlaceBlock = true;
        {
            if (useMaterial == Material.AIR) { // Can't place air
                return;
            }

            //Check if the player is allowed to place blocks based on their game mode
            if (player.getGameMode() == GameMode.SPECTATOR) {
                canPlaceBlock = false; //Spectators can't place blocks
            } else if (player.getGameMode() == GameMode.ADVENTURE) {
                //Check if the block can placed on the block
                canPlaceBlock = usedItem.getMeta().getCanPlaceOn().contains(interactedBlock);
            }
        }

        // Get the newly placed block position
        final int offsetX = blockFace == BlockFace.WEST ? -1 : blockFace == BlockFace.EAST ? 1 : 0;
        final int offsetY = blockFace == BlockFace.BOTTOM ? -1 : blockFace == BlockFace.TOP ? 1 : 0;
        final int offsetZ = blockFace == BlockFace.NORTH ? -1 : blockFace == BlockFace.SOUTH ? 1 : 0;

        blockPosition.add(offsetX, offsetY, offsetZ);

        if (!canPlaceBlock) {
            if (useMaterial.isBlock()) {
                //Send a block change with AIR as block to keep the client in sync,
                //using refreshChunk results in the client not being in sync
                //after rapid invalid block placements
                player.getPlayerConnection().sendPacket(new BlockChangePacket(blockPosition, Block.AIR.stateId()));
            }
            return;
        }

        final Chunk chunk = instance.getChunkAt(blockPosition);

        Check.stateCondition(!ChunkUtils.isLoaded(chunk),
                "A player tried to place a block in the border of a loaded chunk " + blockPosition);

        // The concerned chunk will be send to the player if an error occur
        // This will ensure that the player has the correct version of the chunk
        boolean refreshChunk = false;

        if (useMaterial.isBlock()) {
            if (!chunk.isReadOnly()) {
                final Block placedBlock = useMaterial.getBlock();
                final Set<Entity> entities = instance.getChunkEntities(chunk);
                // Check if the player is trying to place a block in an entity
                boolean intersect = player.getBoundingBox().intersect(blockPosition);
                if (!intersect && placedBlock.isSolid()) {
                    // TODO push entities too close to the position
                    for (Entity entity : entities) {
                        // 'player' has already been checked
                        if (entity == player ||
                                entity.getEntityType() == EntityType.ITEM)
                            continue;

                        intersect = entity.getBoundingBox().intersect(blockPosition);
                        if (intersect)
                            break;
                    }
                }

                if (!intersect) {

                    // BlockPlaceEvent check
                    PlayerBlockPlaceEvent playerBlockPlaceEvent = new PlayerBlockPlaceEvent(player, placedBlock, blockPosition, packet.hand);
                    playerBlockPlaceEvent.consumeBlock(player.getGameMode() != GameMode.CREATIVE);

                    EventDispatcher.call(playerBlockPlaceEvent);
                    if (!playerBlockPlaceEvent.isCancelled()) {

                        // BlockPlacementRule check
                        Block resultBlock = playerBlockPlaceEvent.getBlock();
                        final BlockPlacementRule blockPlacementRule = BLOCK_MANAGER.getBlockPlacementRule(resultBlock);
                        if (blockPlacementRule != null) {
                            // Get id from block placement rule instead of the event
                            resultBlock = blockPlacementRule.blockPlace(instance, resultBlock, blockFace, blockPosition, player);
                        }
                        final boolean placementRuleCheck = resultBlock != null;
                        if (placementRuleCheck) {
                            // Place the block
                            instance.placeBlock(player, resultBlock, blockPosition);
                            // Block consuming
                            if (playerBlockPlaceEvent.doesConsumeBlock()) {
                                // Consume the block in the player's hand
                                final ItemStack newUsedItem = usedItem.getStackingRule().apply(usedItem, usedItem.getAmount() - 1);
                                playerInventory.setItemInHand(hand, newUsedItem);
                            }
                        } else {
                            refreshChunk = true;
                        }
                    } else {
                        refreshChunk = true;
                    }
                } else {
                    refreshChunk = true;
                }
            } else {
                refreshChunk = true;
            }
        } else {
            // Player didn't try to place a block but interacted with one
            final BlockPosition usePosition = blockPosition.clone().subtract(offsetX, offsetY, offsetZ);
            PlayerUseItemOnBlockEvent event = new PlayerUseItemOnBlockEvent(player, hand, usedItem, usePosition, direction);
            EventDispatcher.call(event);
            refreshChunk = true;
        }

        // Refresh chunk section if needed
        if (refreshChunk) {
            chunk.sendChunkSectionUpdate(ChunkUtils.getSectionAt(blockPosition.getY()), player);
        }
    }

}
