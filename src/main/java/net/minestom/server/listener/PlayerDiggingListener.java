package net.minestom.server.listener;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.PlayerMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.server.play.AcknowledgePlayerDiggingPacket;
import org.jetbrains.annotations.NotNull;

public final class PlayerDiggingListener {

    public static void playerDiggingListener(ClientPlayerDiggingPacket packet, Player player) {
        final ClientPlayerDiggingPacket.Status status = packet.status();
        final Point blockPosition = packet.blockPosition();
        final Instance instance = player.getInstance();
        if (instance == null) return;

        DiggingResult diggingResult = null;
        if (status == ClientPlayerDiggingPacket.Status.STARTED_DIGGING) {
            diggingResult = startDigging(player, instance, blockPosition);
        } else if (status == ClientPlayerDiggingPacket.Status.CANCELLED_DIGGING) {
            diggingResult = cancelDigging(instance, blockPosition);
        } else if (status == ClientPlayerDiggingPacket.Status.FINISHED_DIGGING) {
            diggingResult = finishDigging(player, instance, blockPosition);
        } else if (status == ClientPlayerDiggingPacket.Status.DROP_ITEM_STACK) {
            dropStack(player);
        } else if (status == ClientPlayerDiggingPacket.Status.DROP_ITEM) {
            dropSingle(player);
        } else if (status == ClientPlayerDiggingPacket.Status.UPDATE_ITEM_STATE) {
            updateItemState(player);
        } else if (status == ClientPlayerDiggingPacket.Status.SWAP_ITEM_HAND) {
            swapItemHand(player);
        }

        // Acknowledge start/cancel/finish digging status
        if (diggingResult != null) {
            player.getPlayerConnection().sendPacket(new AcknowledgePlayerDiggingPacket(blockPosition, diggingResult.block,
                    status, diggingResult.success));
        }
    }

    private static DiggingResult startDigging(Player player, Instance instance, Point blockPosition) {
        final Block block = instance.getBlock(blockPosition);
        final GameMode gameMode = player.getGameMode();

        if (gameMode == GameMode.SPECTATOR) {
            // Spectators can't break blocks
            return new DiggingResult(block, false);
        } else if (gameMode == GameMode.ADVENTURE) {
            // Check if the item can break the block with the current item
            final ItemStack itemInMainHand = player.getItemInMainHand();
            if (!itemInMainHand.getMeta().getCanDestroy().contains(block)) {
                return new DiggingResult(block, false);
            }
        } else if (gameMode == GameMode.CREATIVE) {
            return breakBlock(instance, player, blockPosition, block);
        }

        // Survival digging
        // FIXME: verify mineable tag and enchantment
        final boolean instantBreak = player.isInstantBreak() || block.registry().hardness() == 0;
        if (!instantBreak) {
            PlayerStartDiggingEvent playerStartDiggingEvent = new PlayerStartDiggingEvent(player, block, blockPosition);
            EventDispatcher.call(playerStartDiggingEvent);
            return new DiggingResult(block, !playerStartDiggingEvent.isCancelled());
        }
        // Client only send a single STARTED_DIGGING when insta-break is enabled
        return breakBlock(instance, player, blockPosition, block);
    }

    private static DiggingResult cancelDigging(Instance instance, Point blockPosition) {
        final Block block = instance.getBlock(blockPosition);
        return new DiggingResult(block, true);
    }

    private static DiggingResult finishDigging(Player player, Instance instance, Point blockPosition) {
        final Block block = instance.getBlock(blockPosition);
        // TODO sanity check
        return breakBlock(instance, player, blockPosition, block);
    }

    private static void dropStack(Player player) {
        final ItemStack droppedItemStack = player.getInventory().getItemInMainHand();
        dropItem(player, droppedItemStack, ItemStack.AIR);
    }

    private static void dropSingle(Player player) {
        final ItemStack handItem = player.getInventory().getItemInMainHand();
        final StackingRule stackingRule = handItem.getStackingRule();
        final int handAmount = stackingRule.getAmount(handItem);
        if (handAmount <= 1) {
            // Drop the whole item without copy
            dropItem(player, handItem, ItemStack.AIR);
        } else {
            // Drop a single item
            dropItem(player,
                    stackingRule.apply(handItem, 1), // Single dropped item
                    stackingRule.apply(handItem, handAmount - 1)); // Updated hand
        }
    }

    private static void updateItemState(Player player) {
        PlayerMeta meta = player.getEntityMeta();
        if (!meta.isHandActive()) return;
        Player.Hand hand = meta.getActiveHand();

        player.refreshEating(null);
        player.triggerStatus((byte) 9);

        ItemUpdateStateEvent itemUpdateStateEvent = player.callItemUpdateStateEvent(hand);
        if (itemUpdateStateEvent == null) {
            player.refreshActiveHand(true, false, false);
        } else {
            final boolean isOffHand = itemUpdateStateEvent.getHand() == Player.Hand.OFF;
            player.refreshActiveHand(itemUpdateStateEvent.hasHandAnimation(), isOffHand, false);
        }
    }

    private static void swapItemHand(Player player) {
        final PlayerInventory inventory = player.getInventory();
        final ItemStack mainHand = inventory.getItemInMainHand();
        final ItemStack offHand = inventory.getItemInOffHand();
        PlayerSwapItemEvent swapItemEvent = new PlayerSwapItemEvent(player, offHand, mainHand);
        EventDispatcher.callCancellable(swapItemEvent, () -> {
            inventory.setItemInMainHand(swapItemEvent.getMainHandItem());
            inventory.setItemInOffHand(swapItemEvent.getOffHandItem());
        });
    }

    private static DiggingResult breakBlock(Instance instance,
                                            Player player,
                                            Point blockPosition, Block previousBlock) {
        // Unverified block break, client is fully responsible
        final boolean success = instance.breakBlock(player, blockPosition);
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

    private static void dropItem(@NotNull Player player,
                                 @NotNull ItemStack droppedItem, @NotNull ItemStack handItem) {
        final PlayerInventory playerInventory = player.getInventory();
        if (player.dropItem(droppedItem)) {
            playerInventory.setItemInMainHand(handItem);
        } else {
            playerInventory.update();
        }
    }

    private static final class DiggingResult {
        public final Block block;
        public final boolean success;

        public DiggingResult(Block block, boolean success) {
            this.block = block;
            this.success = success;
        }
    }
}
