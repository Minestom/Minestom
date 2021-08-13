package net.minestom.server.listener;

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
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

public class PlayerDiggingListener {

    public static void playerDiggingListener(ClientPlayerDiggingPacket packet, Player player) {
        final ClientPlayerDiggingPacket.Status status = packet.status;
        final Point blockPosition = packet.blockPosition;

        final Instance instance = player.getInstance();

        if (instance == null)
            return;

        if (status == ClientPlayerDiggingPacket.Status.STARTED_DIGGING) {
            final Block block = instance.getBlock(blockPosition);

            //Check if the player is allowed to break blocks based on their game mode
            if (player.getGameMode() == GameMode.SPECTATOR) {
                sendAcknowledgePacket(player, blockPosition, block,
                        ClientPlayerDiggingPacket.Status.STARTED_DIGGING, false);
                return; //Spectators can't break blocks
            } else if (player.getGameMode() == GameMode.ADVENTURE) {
                //Check if the item can break the block with the current item
                ItemStack itemInMainHand = player.getItemInMainHand();
                Block destroyedBlock = instance.getBlock(blockPosition);
                if (!itemInMainHand.getMeta().getCanDestroy().contains(destroyedBlock)) {
                    sendAcknowledgePacket(player, blockPosition, block,
                            ClientPlayerDiggingPacket.Status.STARTED_DIGGING, false);
                    return;
                }
            }

            final boolean instantBreak = player.isCreative() ||
                    player.isInstantBreak() ||
                    block.registry().hardness() == 0;

            if (instantBreak) {
                // No need to check custom block
                breakBlock(instance, player, blockPosition, block, status);
            } else {
                PlayerStartDiggingEvent playerStartDiggingEvent = new PlayerStartDiggingEvent(player, block, blockPosition);
                EventDispatcher.call(playerStartDiggingEvent);

                sendAcknowledgePacket(player, blockPosition, block,
                        ClientPlayerDiggingPacket.Status.STARTED_DIGGING, !playerStartDiggingEvent.isCancelled());
            }

        } else if (status == ClientPlayerDiggingPacket.Status.CANCELLED_DIGGING) {

            final Block block = instance.getBlock(blockPosition);
            sendAcknowledgePacket(player, blockPosition, block,
                    ClientPlayerDiggingPacket.Status.CANCELLED_DIGGING, true);

        } else if (status == ClientPlayerDiggingPacket.Status.FINISHED_DIGGING) {

            final Block block = instance.getBlock(blockPosition);
            // Vanilla block
            breakBlock(instance, player, blockPosition, block, status);

        } else if (status == ClientPlayerDiggingPacket.Status.DROP_ITEM_STACK) {

            final ItemStack droppedItemStack = player.getInventory().getItemInMainHand();
            dropItem(player, droppedItemStack, ItemStack.AIR);

        } else if (status == ClientPlayerDiggingPacket.Status.DROP_ITEM) {

            final int dropAmount = 1;

            ItemStack handItem = player.getInventory().getItemInMainHand();
            final StackingRule stackingRule = handItem.getStackingRule();
            final int handAmount = stackingRule.getAmount(handItem);

            if (handAmount <= dropAmount) {
                // Drop the whole item without copy
                dropItem(player, handItem, ItemStack.AIR);
            } else {
                // Drop a single item, need a copy
                ItemStack droppedItemStack2 = stackingRule.apply(handItem, dropAmount);

                handItem = stackingRule.apply(handItem, handAmount - dropAmount);

                dropItem(player, droppedItemStack2, handItem);
            }

        } else if (status == ClientPlayerDiggingPacket.Status.UPDATE_ITEM_STATE) {
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

        } else if (status == ClientPlayerDiggingPacket.Status.SWAP_ITEM_HAND) {

            final PlayerInventory playerInventory = player.getInventory();
            final ItemStack mainHand = playerInventory.getItemInMainHand();
            final ItemStack offHand = playerInventory.getItemInOffHand();

            PlayerSwapItemEvent swapItemEvent = new PlayerSwapItemEvent(player, offHand, mainHand);
            EventDispatcher.callCancellable(swapItemEvent, () -> {
                playerInventory.setItemInMainHand(swapItemEvent.getMainHandItem());
                playerInventory.setItemInOffHand(swapItemEvent.getOffHandItem());
            });

        }
    }

    private static void breakBlock(Instance instance,
                                   Player player,
                                   Point blockPosition, Block block,
                                   ClientPlayerDiggingPacket.Status status) {
        // Unverified block break, client is fully responsible
        final boolean result = instance.breakBlock(player, blockPosition);

        final Block updatedBlock = instance.getBlock(blockPosition);

        // Send acknowledge packet to allow or cancel the digging process
        sendAcknowledgePacket(player, blockPosition, updatedBlock, status, result);

        if (!result) {
            if (block.isSolid()) {
                final var playerPosition = player.getPosition();
                // Teleport the player back if he broke a solid block just below him
                if (playerPosition.sub(0, 1, 0).samePoint(blockPosition))
                    player.teleport(playerPosition);
            }
        }
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

    /**
     * Sends an {@link AcknowledgePlayerDiggingPacket} to a connection.
     *
     * @param player        the player
     * @param blockPosition the block position
     * @param block         the block
     * @param status        the status of the digging
     * @param success       true to notify of a success, false otherwise
     */
    private static void sendAcknowledgePacket(@NotNull Player player, @NotNull Point blockPosition, Block block,
                                              @NotNull ClientPlayerDiggingPacket.Status status, boolean success) {
        player.getPlayerConnection().sendPacket(new AcknowledgePlayerDiggingPacket(blockPosition, block.stateId(), status, success));
    }
}
