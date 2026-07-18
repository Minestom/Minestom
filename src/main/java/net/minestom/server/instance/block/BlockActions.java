package net.minestom.server.instance.block;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerCancelDiggingEvent;
import net.minestom.server.event.player.PlayerFinishDiggingEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.BlockPredicates;
import net.minestom.server.item.component.ItemBlockState;
import net.minestom.server.item.component.Tool;
import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.server.network.packet.server.play.AcknowledgeBlockChangePacket;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.network.packet.server.play.BlockEntityDataPacket;
import net.minestom.server.utils.block.BlockBreakCalculation;
import net.minestom.server.utils.block.BlockUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.Nullable;

/**
 * The block placement and digging pipelines, parameterized by a {@link World}.
 * <p>
 * {@code BlockPlacementListener} and {@code PlayerActionListener} run them against the player's instance. Other
 * worlds (virtual worlds, block overlays) supply their own {@link World} and get the same flow - target
 * resolution, events, acknowledgements, client resyncs - with only the storage swapped.
 */
public final class BlockActions {
    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    private BlockActions() {
    }

    /**
     * The world a pipeline runs against. Defaults resolve every operation through {@link #instance()}; a
     * virtual world overrides the block access and mutations, and keeps the instance for event context.
     */
    public interface World extends Block.Getter {
        /**
         * The instance carried by events and used for dimension bounds and the world border.
         */
        Instance instance();

        /**
         * Whether the chunk holding {@code point} is loaded.
         */
        default boolean isLoaded(Point point) {
            return ChunkUtils.isLoaded(instance().getChunkAt(point));
        }

        /**
         * Whether blocks at {@code point} cannot be modified.
         */
        default boolean isReadOnly(Point point) {
            final Chunk chunk = instance().getChunkAt(point);
            return chunk != null && chunk.isReadOnly();
        }

        /**
         * The entity colliding with {@code block} placed at {@code position} by {@code placer}, or {@code null}.
         */
        default @Nullable Entity blockingEntityAt(Point position, Block block, Player placer) {
            return CollisionUtils.canPlaceBlockAt(instance(), position, block);
        }

        /**
         * Applies a player placement.
         */
        default void placeBlock(BlockHandler.PlayerPlacement placement, boolean doBlockUpdates) {
            instance().placeBlock(placement, doBlockUpdates);
        }

        /**
         * Breaks the block at {@code position}, firing whatever the world fires for a player break.
         *
         * @return false if the break was refused
         */
        default boolean breakBlock(Player player, Point position, BlockFace blockFace) {
            return instance().breakBlock(player, position, blockFace);
        }
    }

    /**
     * The instance-backed {@link World} the stock listeners use.
     */
    public static World world(Instance instance) {
        return new World() {
            @Override
            public Instance instance() {
                return instance;
            }

            @Override
            public Block getBlock(int x, int y, int z, Condition condition) {
                return instance.getBlock(x, y, z, condition);
            }
        };
    }

    /**
     * Runs the placement pipeline: interaction events, placement target resolution, placement rules, the place
     * event, the write through {@code world}, and the acknowledgement/resync traffic.
     */
    public static void place(World world, Player player, ClientPlayerBlockPlacementPacket packet) {
        final PlayerHand hand = packet.hand();
        final BlockFace blockFace = packet.blockFace();
        Point blockPosition = packet.blockPosition();
        final Instance instance = world.instance();

        // Prevent outdated/modified client data
        if (!world.isLoaded(blockPosition)) {
            // Client tried to place a block in an unloaded chunk; ignore the request but still ack to reset the client prediction
            player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
            return;
        }

        final ItemStack usedItem = player.getItemInHand(hand);
        final Block interactedBlock = world.getBlock(blockPosition);

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

            var placementBlock = world.getBlock(placementPosition);
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
            rollback(player, hand, placementPosition, world.getBlock(placementPosition), packet.sequence());
            return;
        }

        Check.stateCondition(!world.isLoaded(placementPosition),
                "A player tried to place a block in the border of a loaded chunk {0}", placementPosition);
        if (world.isReadOnly(placementPosition)) {
            rollback(player, hand, placementPosition, world.getBlock(placementPosition), packet.sequence());
            return;
        }

        final ItemBlockState blockState = usedItem.get(DataComponents.BLOCK_STATE, ItemBlockState.EMPTY);
        final Block placedBlock = blockState.apply(useMaterial.block());

        Entity collisionEntity = world.blockingEntityAt(placementPosition, placedBlock, player);
        if (collisionEntity != null) {
            // If a player is trying to place a block on themselves, the client sends a block change but does not set
            // the block on its own client, so it only needs an acknowledgement.
            if (collisionEntity == player) {
                player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
                return;
            }
            // Otherwise correct the block where the server and client bounding boxes differ, with a targeted block change instead of a chunk resend.
            rollback(player, hand, placementPosition, world.getBlock(placementPosition), packet.sequence());
            return;
        }

        // BlockPlaceEvent check
        PlayerBlockPlaceEvent playerBlockPlaceEvent = new PlayerBlockPlaceEvent(player, instance, placedBlock, blockFace, placementPosition.asBlockVec(), cursorPosition, packet.hand());
        playerBlockPlaceEvent.consumeBlock(player.getGameMode() != GameMode.CREATIVE);
        playerBlockPlaceEvent.setDoBlockUpdates(blockState.equals(useMaterial.prototype().get(DataComponents.BLOCK_STATE, ItemBlockState.EMPTY)));
        EventDispatcher.call(playerBlockPlaceEvent);
        if (playerBlockPlaceEvent.isCancelled()) {
            rollback(player, hand, placementPosition, world.getBlock(placementPosition), packet.sequence());
            return;
        }

        // Place the block
        Block resultBlock = playerBlockPlaceEvent.getBlock();
        world.placeBlock(new BlockHandler.PlayerPlacement(resultBlock, world.getBlock(placementPosition), instance, placementPosition, player, hand, blockFace,
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

    /**
     * Runs the digging pipeline for a {@code STARTED_DIGGING}/{@code CANCELLED_DIGGING}/{@code FINISHED_DIGGING}
     * action: digging events, break checks, the break through {@code world}, and the acknowledgement traffic.
     * Other action statuses are not block actions and are rejected.
     */
    public static void dig(World world, Player player, ClientPlayerActionPacket packet) {
        final ClientPlayerActionPacket.Status status = packet.status();
        final Point blockPosition = packet.blockPosition();
        if (!world.isLoaded(blockPosition)) return;

        final DiggingResult diggingResult = switch (status) {
            case STARTED_DIGGING -> startDigging(world, player, blockPosition, packet.blockFace());
            case CANCELLED_DIGGING -> cancelDigging(world, player, blockPosition);
            case FINISHED_DIGGING -> finishDigging(world, player, blockPosition, packet.blockFace());
            default -> throw new IllegalArgumentException("Not a digging status: " + status);
        };
        player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
        if (!diggingResult.success()) {
            // Refresh block on player screen in case it had special data (like a sign)
            var blockEntityType = diggingResult.block().registry().blockEntityType();
            if (blockEntityType != null) {
                final CompoundBinaryTag data = BlockUtils.extractClientNbt(diggingResult.block());
                player.sendPacketToViewersAndSelf(new BlockEntityDataPacket(blockPosition, blockEntityType, data));
            }
        }
    }

    private static DiggingResult startDigging(World world, Player player, Point blockPosition, BlockFace blockFace) {
        final Block block = world.getBlock(blockPosition);

        // Prevent spectators and check players in adventure mode
        if (shouldPreventBreaking(player, block)) {
            return new DiggingResult(block, false);
        }

        final int breakTicks = BlockBreakCalculation.breakTicks(block, player);
        final boolean instantBreak = breakTicks == 0;
        if (!instantBreak) {
            PlayerStartDiggingEvent playerStartDiggingEvent = new PlayerStartDiggingEvent(player, world.instance(), block, blockPosition.asBlockVec(), blockFace);
            EventDispatcher.call(playerStartDiggingEvent);
            return new DiggingResult(block, !playerStartDiggingEvent.isCancelled());
        }
        // Client only sends a single STARTED_DIGGING when insta-break is enabled
        return breakBlock(world, player, blockPosition, block, blockFace);
    }

    private static DiggingResult cancelDigging(World world, Player player, Point blockPosition) {
        final Block block = world.getBlock(blockPosition);

        PlayerCancelDiggingEvent playerCancelDiggingEvent = new PlayerCancelDiggingEvent(player, world.instance(), block, blockPosition.asBlockVec());
        EventDispatcher.call(playerCancelDiggingEvent);
        return new DiggingResult(block, true);
    }

    private static DiggingResult finishDigging(World world, Player player, Point blockPosition, BlockFace blockFace) {
        final Block block = world.getBlock(blockPosition);

        if (shouldPreventBreaking(player, block)) {
            return new DiggingResult(block, false);
        }

        final int breakTicks = BlockBreakCalculation.breakTicks(block, player);
        // Realistically shouldn't happen, but a hacked client can send any packet, also illegal ones
        // If the block is unbreakable, prevent a hacked client from breaking it!
        if (breakTicks == BlockBreakCalculation.UNBREAKABLE) {
            PlayerCancelDiggingEvent playerCancelDiggingEvent = new PlayerCancelDiggingEvent(player, world.instance(), block, blockPosition.asBlockVec());
            EventDispatcher.call(playerCancelDiggingEvent);
            return new DiggingResult(block, false);
        }
        // TODO maybe add a check if the player has spent enough time mining the block.
        //   a hacked client could send START_DIGGING and FINISH_DIGGING to instamine any block

        PlayerFinishDiggingEvent playerFinishDiggingEvent = new PlayerFinishDiggingEvent(player, world.instance(), block, blockPosition.asBlockVec());
        EventDispatcher.call(playerFinishDiggingEvent);

        return breakBlock(world, player, blockPosition, playerFinishDiggingEvent.getBlock(), blockFace);
    }

    private static boolean shouldPreventBreaking(Player player, Block block) {
        final ItemStack itemInMainHand = player.getItemInMainHand();

        return switch (player.getGameMode()) {
            // Spectators can't break blocks
            case SPECTATOR -> true;
            // Check if the currently held item can break the block
            case ADVENTURE -> !itemInMainHand
                    .get(DataComponents.CAN_BREAK, BlockPredicates.NEVER)
                    .test(block);
            // Certain tools (swords, tridents, maces) can't break blocks in creative
            case CREATIVE -> {
                final Tool tool = itemInMainHand.get(DataComponents.TOOL);
                yield tool != null && !tool.canDestroyBlocksInCreative();
            }
            default -> false;
        };
    }

    private static DiggingResult breakBlock(World world,
                                            Player player,
                                            Point blockPosition, Block previousBlock, BlockFace blockFace) {
        // Unverified block break, client is fully responsible
        final boolean success = world.breakBlock(player, blockPosition, blockFace);
        final Block updatedBlock = world.getBlock(blockPosition);
        if (!success) {
            if (previousBlock.isSolid()) {
                final Pos playerPosition = player.getPosition();
                // Teleport the player back if he broke a solid block just below him
                if (playerPosition.sub(0, 1, 0).samePoint(blockPosition)) {
                    player.teleport(playerPosition);
                }
            }
        }
        return new DiggingResult(updatedBlock, success);
    }

    private record DiggingResult(Block block, boolean success) {
    }
}
