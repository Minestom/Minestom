package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
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
        if (instance == null) return;

        final ItemStack usedItem = player.getItemInHand(hand);

        // Interact at block
        final boolean cancel = usedItem.onUseOnBlock(player, hand, blockPosition, direction);
        PlayerBlockInteractEvent playerBlockInteractEvent = new PlayerBlockInteractEvent(player, blockPosition, hand, blockFace);
        playerBlockInteractEvent.setCancelled(cancel);
        playerBlockInteractEvent.setBlockingItemUse(cancel);

        player.callCancellableEvent(PlayerBlockInteractEvent.class, playerBlockInteractEvent, () -> {
            final CustomBlock customBlock = instance.getCustomBlock(blockPosition);
            if (customBlock == null) return;

            final Data data = instance.getBlockData(blockPosition);
            final boolean blocksItem = customBlock.onInteract(player, hand, blockPosition, data);
            if (blocksItem) playerBlockInteractEvent.setBlockingItemUse(true);
        });

        if (playerBlockInteractEvent.isBlockingItemUse()) return;

        final Material useMaterial = usedItem.getMaterial();
        if (useMaterial == Material.AIR) return;

        // Verify if the player can place the block
//        if (player.getGameMode() == GameMode.SPECTATOR || !usedItem.canPlaceOn(instance.getBlock(blockPosition).getName())) { Investigate what canPlaceOn does.
        if (player.getGameMode() == GameMode.SPECTATOR) {
            BlockChangePacket blockChangePacket = new BlockChangePacket();
            blockChangePacket.blockPosition = blockPosition;
            blockChangePacket.blockStateId = Block.AIR.getBlockId();
            player.getPlayerConnection().sendPacket(blockChangePacket);
            return;
        }

        // Get the newly placed block position
        final int offsetX = blockFace == BlockFace.WEST ? -1 : blockFace == BlockFace.EAST ? 1 : 0;
        final int offsetY = blockFace == BlockFace.BOTTOM ? -1 : blockFace == BlockFace.TOP ? 1 : 0;
        final int offsetZ = blockFace == BlockFace.NORTH ? -1 : blockFace == BlockFace.SOUTH ? 1 : 0;

        blockPosition.add(offsetX, offsetY, offsetZ);

        final Chunk chunk = instance.getChunkAt(blockPosition);
        if (!ChunkUtils.isLoaded(chunk)) return;

        if (!useMaterial.isBlock()) {
            final BlockPosition usePosition = blockPosition.clone().subtract(offsetX, offsetY, offsetZ);
            PlayerUseItemOnBlockEvent event = new PlayerUseItemOnBlockEvent(player, hand, usedItem, usePosition, direction);
            player.callEvent(PlayerUseItemOnBlockEvent.class, event);

            chunk.sendChunkSectionUpdate(ChunkUtils.getSectionAt(blockPosition.getY()), player);
            return;
        }

        if (chunk.isReadOnly()) {
            chunk.sendChunkSectionUpdate(ChunkUtils.getSectionAt(blockPosition.getY()), player);
            return;
        }

        final Block block = useMaterial.getBlock();
        final Set<Entity> entities = instance.getChunkEntities(chunk);
        // Check if the player is trying to place a block in an entity
        boolean intersect = player.getBoundingBox().intersect(blockPosition);
        if (!intersect && block.isSolid()) {
            // TODO push entities too close to the position
            for (Entity entity : entities) {
                // 'player' has already been checked
                if (entity == player || entity.getEntityType() == EntityType.ITEM) continue;

                intersect = entity.getBoundingBox().intersect(blockPosition);
                if (intersect) {
                    chunk.sendChunkSectionUpdate(ChunkUtils.getSectionAt(blockPosition.getY()), player);
                    return;
                }
            }
        } else {
            chunk.sendChunkSectionUpdate(ChunkUtils.getSectionAt(blockPosition.getY()), player);
            return;
        }

        // BlockPlaceEvent check
        PlayerBlockPlaceEvent playerBlockPlaceEvent = new PlayerBlockPlaceEvent(player, block, blockPosition, packet.hand);
        playerBlockPlaceEvent.consumeBlock(player.getGameMode() != GameMode.CREATIVE);
        player.callEvent(PlayerBlockPlaceEvent.class, playerBlockPlaceEvent);

        if (playerBlockPlaceEvent.isCancelled()) {
            chunk.sendChunkSectionUpdate(ChunkUtils.getSectionAt(blockPosition.getY()), player);
            return;
        }

        // BlockPlacementRule check
        short blockStateId = playerBlockPlaceEvent.getBlockStateId();
        short customBlockId = playerBlockPlaceEvent.getCustomBlockId();
        Data blockData = playerBlockPlaceEvent.getBlockData(); // Possibly null

        // Place the block
        instance.placeBlock(player, chunk, blockFace, blockPosition, blockStateId, customBlockId, blockData);

        // Block consuming
        if (playerBlockPlaceEvent.doesConsumeBlock()) {
            // Consume the block in the player's hand
            final ItemStack newUsedItem = usedItem.consume(1);
            if (newUsedItem != null) playerInventory.setItemInHand(hand, newUsedItem);
        }

        player.getInventory().refreshSlot(player.getHeldSlot()); // Perhaps this should be inside the if statement? What could update if the block wasn't consumed?
    }

}
