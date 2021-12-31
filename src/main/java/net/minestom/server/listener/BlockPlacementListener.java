package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerBlockUpdateNeighborEvent;
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
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.validate.Check;

import java.util.HashSet;
import java.util.Set;

public class BlockPlacementListener {
    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();
    public static final int MAX_NEIGHBOR_UPDATE_LENGTH = 4;
    public static final Vec[] DIRS = { new Vec(1, 0, 0), new Vec(-1, 0, 0),
            new Vec(0, 1, 0), new Vec(0, -1, 0),
            new Vec(0, 0, 1), new Vec(0, 0, -1)};

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

        // Interact at block
        // FIXME: onUseOnBlock
        PlayerBlockInteractEvent playerBlockInteractEvent = new PlayerBlockInteractEvent(player, hand, interactedBlock, blockPosition, blockFace);
        EventDispatcher.call(playerBlockInteractEvent);
        boolean cancelBlockPlacement = playerBlockInteractEvent.isBlockingItemUse();
        boolean refreshBlock = cancelBlockPlacement;
        if (!playerBlockInteractEvent.isCancelled()) {
            final var handler = interactedBlock.handler();
            if (handler != null) {
                cancelBlockPlacement |= !handler.onInteract(new BlockHandler.Interaction(interactedBlock, instance, blockPosition, player, hand));
                refreshBlock = cancelBlockPlacement;
            }
            if (playerBlockInteractEvent.getBlock() != interactedBlock) {
                instance.setBlock(blockPosition, playerBlockInteractEvent.getBlock());
                refreshBlock = false;
            }
        }
        if (refreshBlock) {
            player.getPlayerConnection().sendPacket(new BlockChangePacket(blockPosition, interactedBlock));
        }
        if (cancelBlockPlacement) {
            return;
        }

        final Material useMaterial = usedItem.getMaterial();
        if (!useMaterial.isBlock()) {
            // Player didn't try to place a block but interacted with one
            PlayerUseItemOnBlockEvent event = new PlayerUseItemOnBlockEvent(player, hand, usedItem, blockPosition, blockFace);
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
            canPlaceBlock = usedItem.getMeta().getCanPlaceOn().contains(interactedBlock);
        }

        // If the block is air, don't offset in order to mimic vanilla's "replace" function
        // when players break and replace blocks during the same tick
        int offsetX = 0, offsetY = 0, offsetZ = 0;
        if(!instance.getBlock(blockPosition).isAir()) {
            // Get the newly placed block position
            offsetX = blockFace == BlockFace.WEST ? -1 : blockFace == BlockFace.EAST ? 1 : 0;
            offsetY = blockFace == BlockFace.BOTTOM ? -1 : blockFace == BlockFace.TOP ? 1 : 0;
            offsetZ = blockFace == BlockFace.NORTH ? -1 : blockFace == BlockFace.SOUTH ? 1 : 0;
        }
        final Point placementPosition = blockPosition.add(offsetX, offsetY, offsetZ);

        if (!canPlaceBlock) {
            // Send a block change with AIR as block to keep the client in sync,
            // using refreshChunk results in the client not being in sync
            // after rapid invalid block placements
            player.getPlayerConnection().sendPacket(new BlockChangePacket(placementPosition, Block.AIR));
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
        final Set<Entity> entities = instance.getChunkEntities(chunk);
        // Check if the player is trying to place a block in an entity
        boolean intersect = player.getBoundingBox().intersectWithBlock(placementPosition);
        if (!intersect && placedBlock.isSolid()) {
            // TODO push entities too close to the position
            for (Entity entity : entities) {
                // 'player' has already been checked
                if (entity == player ||
                        entity.getEntityType() == EntityType.ITEM)
                    continue;
                // Marker Armor Stands should not prevent block placement
                if (entity.getEntityMeta() instanceof ArmorStandMeta armorStandMeta) {
                    if (armorStandMeta.isMarker()) continue;
                }
                intersect = entity.getBoundingBox().intersectWithBlock(placementPosition);
                if (intersect)
                    break;
            }
        }
        if (intersect) {
            refresh(player, chunk);
            return;
        }
        // BlockPlaceEvent check
        PlayerBlockPlaceEvent playerBlockPlaceEvent = new PlayerBlockPlaceEvent(player, placedBlock, blockFace, placementPosition,
                new Vec(packet.cursorPositionX(), packet.cursorPositionY(), packet.cursorPositionZ()), packet.hand());
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
        if (!instance.placeBlock(new BlockHandler.PlayerPlacement(resultBlock, instance, placementPosition, player, hand, blockFace,
                packet.cursorPositionX(), packet.cursorPositionY(), packet.cursorPositionZ()))) {
            refresh(player, chunk);
            return;
        }
        // Block consuming
        if (playerBlockPlaceEvent.doesConsumeBlock()) {
            // Consume the block in the player's hand
            final ItemStack newUsedItem = usedItem.getStackingRule().apply(usedItem, usedItem.getAmount() - 1);
            playerInventory.setItemInHand(hand, newUsedItem);
        }

        // Update neighbors
        Set<Point> updatedNeighbors = new HashSet<>();
        Set<Point> toUpdate = new HashSet<>();

        toUpdate.add(placementPosition);
        updatedNeighbors.add(placementPosition); //Don't update the block we just placed

        for(int i=0; i<MAX_NEIGHBOR_UPDATE_LENGTH; i++) {
            Set<Point> toUpdateCopy = new HashSet<>(toUpdate);
            toUpdate.clear();

            for(Point pos : toUpdateCopy) {
                for(Vec dir : DIRS) {
                    Point position = pos.add(dir);

                    if(updatedNeighbors.contains(position)) continue;
                    updatedNeighbors.add(position);

                    Block block = instance.getBlock(position);

                    if(block.isAir()) continue;

                    PlayerBlockUpdateNeighborEvent playerBlockUpdateNeighborEvent = new PlayerBlockUpdateNeighborEvent(player, block, position);
                    EventDispatcher.call(playerBlockUpdateNeighborEvent);

                    if (playerBlockUpdateNeighborEvent.getBlock() != block) {
                        instance.setBlock(position, playerBlockUpdateNeighborEvent.getBlock());
                    }

                    if (playerBlockUpdateNeighborEvent.isShouldUpdateNeighbors()) {
                        toUpdate.add(position);
                    }
                }
            }
        }
    }

    private static void refresh(Player player, Chunk chunk) {
        player.getInventory().update();
        chunk.sendChunk(player);
    }
}
