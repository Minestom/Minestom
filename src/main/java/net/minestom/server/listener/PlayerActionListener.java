package net.minestom.server.listener;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.PlayerCancelItemUseEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.BlockPredicates;
import net.minestom.server.item.component.Tool;
import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket;
import net.minestom.server.network.packet.server.play.AcknowledgeBlockChangePacket;
import net.minestom.server.network.packet.server.play.BlockEntityDataPacket;
import net.minestom.server.utils.block.BlockBreakCalculation;
import net.minestom.server.utils.block.BlockUtils;

public final class PlayerActionListener {

    public static void playerActionListener(ClientPlayerActionPacket packet, Player player) {
        final ClientPlayerActionPacket.Status status = packet.status();
        final Point blockPosition = packet.blockPosition();
        final Instance instance = player.getInstance();
        if (instance == null) return;

        DiggingResult diggingResult = null;
        if (status == ClientPlayerActionPacket.Status.STARTED_DIGGING) {
            if (!instance.isChunkLoaded(blockPosition)) return;
            diggingResult = startDigging(player, instance, blockPosition, packet.blockFace());
        } else if (status == ClientPlayerActionPacket.Status.CANCELLED_DIGGING) {
            if (!instance.isChunkLoaded(blockPosition)) return;
            diggingResult = cancelDigging(player, instance, blockPosition);
        } else if (status == ClientPlayerActionPacket.Status.FINISHED_DIGGING) {
            if (!instance.isChunkLoaded(blockPosition)) return;
            diggingResult = finishDigging(player, instance, blockPosition, packet.blockFace());
        } else if (status == ClientPlayerActionPacket.Status.DROP_ITEM_STACK) {
            dropStack(player);
        } else if (status == ClientPlayerActionPacket.Status.DROP_ITEM) {
            dropSingle(player);
        } else if (status == ClientPlayerActionPacket.Status.UPDATE_ITEM_STATE) {
            updateItemState(player);
        } else if (status == ClientPlayerActionPacket.Status.SWAP_ITEM_HAND) {
            swapItemHand(player);
        } else if (status == ClientPlayerActionPacket.Status.STAB) {
            stab(player);
        }
        // Acknowledge start/cancel/finish digging status
        if (diggingResult != null) {
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
    }

    private static DiggingResult startDigging(Player player, Instance instance, Point blockPosition, BlockFace blockFace) {
        final Block block = instance.getBlock(blockPosition);

        // Prevent spectators and check players in adventure mode
        if (shouldPreventBreaking(player, block)) {
            return new DiggingResult(block, false);
        }

        final int breakTicks = BlockBreakCalculation.breakTicks(block, player);
        final boolean instantBreak = breakTicks == 0;
        if (!instantBreak) {
            PlayerStartDiggingEvent playerStartDiggingEvent = new PlayerStartDiggingEvent(player, block, new BlockVec(blockPosition), blockFace);
            EventDispatcher.call(playerStartDiggingEvent);
            return new DiggingResult(block, !playerStartDiggingEvent.isCancelled());
        }
        // Client only sends a single STARTED_DIGGING when insta-break is enabled
        return breakBlock(instance, player, blockPosition, block, blockFace);
    }

    private static DiggingResult cancelDigging(Player player, Instance instance, Point blockPosition) {
        final Block block = instance.getBlock(blockPosition);

        PlayerCancelDiggingEvent playerCancelDiggingEvent = new PlayerCancelDiggingEvent(player, block, new BlockVec(blockPosition));
        EventDispatcher.call(playerCancelDiggingEvent);
        return new DiggingResult(block, true);
    }

    private static DiggingResult finishDigging(Player player, Instance instance, Point blockPosition, BlockFace blockFace) {
        final Block block = instance.getBlock(blockPosition);

        if (shouldPreventBreaking(player, block)) {
            return new DiggingResult(block, false);
        }

        final int breakTicks = BlockBreakCalculation.breakTicks(block, player);
        // Realistically shouldn't happen, but a hacked client can send any packet, also illegal ones
        // If the block is unbreakable, prevent a hacked client from breaking it!
        if (breakTicks == BlockBreakCalculation.UNBREAKABLE) {
            PlayerCancelDiggingEvent playerCancelDiggingEvent = new PlayerCancelDiggingEvent(player, block, new BlockVec(blockPosition));
            EventDispatcher.call(playerCancelDiggingEvent);
            return new DiggingResult(block, false);
        }
        // TODO maybe add a check if the player has spent enough time mining the block.
        //   a hacked client could send START_DIGGING and FINISH_DIGGING to instamine any block

        PlayerFinishDiggingEvent playerFinishDiggingEvent = new PlayerFinishDiggingEvent(player, block, new BlockVec(blockPosition));
        EventDispatcher.call(playerFinishDiggingEvent);

        return breakBlock(instance, player, blockPosition, playerFinishDiggingEvent.getBlock(), blockFace);
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

    private static void dropStack(Player player) {
        final ItemStack droppedItemStack = player.getItemInMainHand();
        dropItem(player, droppedItemStack, ItemStack.AIR);
    }

    private static void dropSingle(Player player) {
        final ItemStack handItem = player.getItemInMainHand();
        final int handAmount = handItem.amount();
        if (handAmount <= 1) {
            // Drop the whole item without copy
            dropItem(player, handItem, ItemStack.AIR);
        } else {
            // Drop a single item
            dropItem(player,
                    handItem.withAmount(1), // Single dropped item
                    handItem.withAmount(handAmount - 1)); // Updated hand
        }
    }

    private static void updateItemState(Player player) {
        LivingEntityMeta meta = player.getLivingEntityMeta();
        if (meta == null || !meta.isHandActive()) return;
        final PlayerHand hand = meta.getActiveHand();

        PlayerCancelItemUseEvent cancelUseEvent = new PlayerCancelItemUseEvent(player, hand, player.getItemInHand(hand), player.getCurrentItemUseTime());
        EventDispatcher.call(cancelUseEvent);

        // Reset server state
        final boolean isOffHand = hand == PlayerHand.OFF;
        player.setShouldFilterClientInput(true);
        player.refreshActiveHand(false, isOffHand, cancelUseEvent.isRiptideSpinAttack());
        player.clearItemUse();
        player.setShouldFilterClientInput(false);
    }

    private static void swapItemHand(Player player) {
        final ItemStack mainHand = player.getItemInMainHand();
        final ItemStack offHand = player.getItemInOffHand();
        PlayerSwapItemEvent swapItemEvent = new PlayerSwapItemEvent(player, offHand, mainHand);
        EventDispatcher.callCancellable(swapItemEvent, () -> {
            player.setItemInMainHand(swapItemEvent.getMainHandItem());
            player.setItemInOffHand(swapItemEvent.getOffHandItem());
        });
    }

    private static DiggingResult breakBlock(Instance instance,
                                            Player player,
                                            Point blockPosition, Block previousBlock, BlockFace blockFace) {
        // Unverified block break, client is fully responsible
        final boolean success = instance.breakBlock(player, blockPosition, blockFace);
        final Block updatedBlock = instance.getBlock(blockPosition);
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

    private static void dropItem(Player player,
                                 ItemStack droppedItem, ItemStack handItem) {
        if (player.dropItem(droppedItem)) {
            player.setItemInMainHand(handItem);
        } else {
            player.getInventory().update();
        }
    }

    private static void stab(Player player) {
        final ItemStack itemInMainHand = player.getItemInMainHand();
        if (!itemInMainHand.has(DataComponents.PIERCING_WEAPON))
            return;
        EventDispatcher.call(new PlayerStabEvent(player));
    }

    private record DiggingResult(Block block, boolean success) {
    }
}
