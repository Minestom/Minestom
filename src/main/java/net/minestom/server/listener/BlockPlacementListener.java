package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
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
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.BlockPredicates;
import net.minestom.server.item.component.ItemBlockState;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.server.network.packet.server.play.AcknowledgeBlockChangePacket;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;

public class BlockPlacementListener {
    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    public static void listener(ClientPlayerBlockPlacementPacket packet, Player player) {
        final PlayerHand hand = packet.hand();
        final BlockFace blockFace = packet.blockFace();
        Point blockPosition = packet.blockPosition();

        final Instance instance = player.getInstance();
        if (instance == null) {
            player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
            return;
        }

        // Prevent outdated/modified client data
        final Chunk interactedChunk = instance.getChunkAt(blockPosition);
        if (!ChunkUtils.isLoaded(interactedChunk)) {
            // Client tried to place a block in an unloaded chunk; ignore the request but still ack to reset the client prediction
            player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
            return;
        }

        final ItemStack usedItem = player.getItemInHand(hand);
        final Block interactedBlock = instance.getBlock(blockPosition);

        final Point cursorPosition = new Vec(packet.cursorPositionX(), packet.cursorPositionY(), packet.cursorPositionZ());

        // Interact at block
        // FIXME: onUseOnBlock
        PlayerBlockInteractEvent playerBlockInteractEvent = new PlayerBlockInteractEvent(player, hand, instance, interactedBlock, blockPosition.asBlockVec(), cursorPosition, blockFace);
        EventDispatcher.call(playerBlockInteractEvent);
        boolean blockUse = playerBlockInteractEvent.isBlockingItemUse();
        if (!playerBlockInteractEvent.isCancelled()) {
            final var handler = interactedBlock.handler();
            if (handler != null) {
                blockUse |= !handler.onInteract(new BlockHandler.Interaction(interactedBlock, instance, blockFace, blockPosition, cursorPosition, player, hand));
            }
        }
        if (blockUse) {
            // If the usage was blocked then the world is already up-to-date (from the prior handlers),
            // So ack the change with the current world state.
            player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
            return;
        }

        final Material useMaterial = usedItem.material();
        if (!useMaterial.isBlock()) {
            // Player didn't try to place a block but interacted with one
            PlayerUseItemOnBlockEvent event = new PlayerUseItemOnBlockEvent(player, hand, usedItem, blockPosition, cursorPosition, blockFace);
            EventDispatcher.call(event);
            // Ack the block change. This is required to reset the client prediction to the server state.
            player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
            return;
        }

        // Verify if the player can place the block
        boolean canPlaceBlock = true;
        // Check if the player is allowed to place blocks based on their game mode
        if (player.getGameMode() == GameMode.SPECTATOR) {
            canPlaceBlock = false; // Spectators can't place blocks
        } else if (player.getGameMode() == GameMode.ADVENTURE) {
            //Check if the block can be placed on the block
            BlockPredicates placePredicate = usedItem.get(DataComponents.CAN_PLACE_ON, BlockPredicates.NEVER);
            canPlaceBlock = placePredicate.test(interactedBlock);
        }


        // Get the newly placed block position
        //todo it feels like it should be possible to have better replacement rules than this, feels pretty scuffed.
        Point placementPosition = blockPosition;
        var interactedPlacementRule = BLOCK_MANAGER.getBlockPlacementRule(interactedBlock);
        if (!interactedBlock.isAir() && (interactedPlacementRule == null || !interactedPlacementRule.isSelfReplaceable(
                new BlockPlacementRule.Replacement(interactedBlock, blockFace, cursorPosition, false, useMaterial)))) {
            // If the block is not replaceable, try to place next to it.
            placementPosition = blockPosition.relative(blockFace);

            var placementBlock = instance.getBlock(placementPosition);
            var placementRule = BLOCK_MANAGER.getBlockPlacementRule(placementBlock);
            if (!placementBlock.registry().isReplaceable() && !(placementRule != null && placementRule.isSelfReplaceable(
                    new BlockPlacementRule.Replacement(placementBlock, blockFace, cursorPosition, true, useMaterial)))) {
                // If the block is still not replaceable, cancel the placement
                canPlaceBlock = false;
            }
        }

        final DimensionType instanceDim = instance.getCachedDimensionType();
        if (placementPosition.y() >= instanceDim.maxY() || placementPosition.y() < instanceDim.minY()) {
            // Placement outside the world's build height (reachable by placing against the top/bottom block); ack to reset the prediction
            player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
            return;
        }

        // Ensure that the final placement position is inside the world border.
        if (!instance.getWorldBorder().inBounds(placementPosition)) {
            canPlaceBlock = false;
        }

        if (!canPlaceBlock) {
            // Keep the client in sync with a targeted block change (plus a held-slot refresh) instead of a chunk resend,
            // which leaves the client desynced after rapid invalid block placements.
            rollback(player, hand, placementPosition, instance.getBlock(placementPosition), packet.sequence());
            return;
        }

        final Chunk chunk = instance.getChunkAt(placementPosition);
        Check.stateCondition(!ChunkUtils.isLoaded(chunk),
                "A player tried to place a block in the border of a loaded chunk {0}", placementPosition);
        if (chunk.isReadOnly()) {
            rollback(player, hand, placementPosition, instance.getBlock(placementPosition), packet.sequence());
            return;
        }

        final ItemBlockState blockState = usedItem.get(DataComponents.BLOCK_STATE, ItemBlockState.EMPTY);
        final Block placedBlock = blockState.apply(useMaterial.block());

        Entity collisionEntity = CollisionUtils.canPlaceBlockAt(instance, placementPosition, placedBlock, player);
        if (collisionEntity != null) {
            // If a player is trying to place a block on themselves, the client sends a block change but does not set
            // the block on its own client, so it only needs an acknowledgement.
            if (collisionEntity == player) {
                player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
                return;
            }
            // Otherwise correct the block where the server and client bounding boxes differ, with a targeted block change instead of a chunk resend.
            rollback(player, hand, placementPosition, instance.getBlock(placementPosition), packet.sequence());
            return;
        }

        // BlockPlaceEvent check
        PlayerBlockPlaceEvent playerBlockPlaceEvent = new PlayerBlockPlaceEvent(player, instance, placedBlock, blockFace, placementPosition.asBlockVec(), cursorPosition, packet.hand());
        playerBlockPlaceEvent.consumeBlock(player.getGameMode() != GameMode.CREATIVE);
        playerBlockPlaceEvent.setDoBlockUpdates(blockState.equals(useMaterial.prototype().get(DataComponents.BLOCK_STATE, ItemBlockState.EMPTY)));
        EventDispatcher.call(playerBlockPlaceEvent);
        if (playerBlockPlaceEvent.isCancelled()) {
            rollback(player, hand, placementPosition, instance.getBlock(placementPosition), packet.sequence());
            return;
        }

        // Place the block
        Block resultBlock = playerBlockPlaceEvent.getBlock();
        instance.placeBlock(new BlockHandler.PlayerPlacement(resultBlock, instance.getBlock(placementPosition), instance, placementPosition, player, hand, blockFace,
                packet.cursorPositionX(), packet.cursorPositionY(), packet.cursorPositionZ()), playerBlockPlaceEvent.shouldDoBlockUpdates());
        player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
        // Block consuming
        if (playerBlockPlaceEvent.doesConsumeBlock()) {
            // Consume the block in the player's hand
            final ItemStack newUsedItem = usedItem.consume(1);
            player.setItemInHand(hand, newUsedItem);
        } else {
            // Prevent invisible item on client: it predicted a decrement that didn't happen, so refresh just that slot.
            final int slot = hand == PlayerHand.OFF ? PlayerInventoryUtils.OFFHAND_SLOT : player.getHeldSlot();
            player.getInventory().sendSlotRefresh(slot, player.getItemInHand(hand));
        }
    }

    // Corrects a rejected placement with a targeted block change instead of resending the whole chunk, and refreshes
    // only the used hand slot so the client's predicted item count stays in sync without a full inventory resend.
    private static void rollback(Player player, PlayerHand hand, Point placementPosition, Block block, int sequence) {
        final int slot = hand == PlayerHand.OFF ? PlayerInventoryUtils.OFFHAND_SLOT : player.getHeldSlot();
        player.getInventory().sendSlotRefresh(slot, player.getItemInHand(hand));
        player.sendPacket(new BlockChangePacket(placementPosition, block));
        player.sendPacket(new AcknowledgeBlockChangePacket(sequence));
    }
}
