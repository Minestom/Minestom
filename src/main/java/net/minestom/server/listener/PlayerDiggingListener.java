package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.server.play.AcknowledgePlayerDiggingPacket;
import net.minestom.server.network.packet.server.play.EntityEffectPacket;
import net.minestom.server.network.packet.server.play.RemoveEntityEffectPacket;
import net.minestom.server.utils.BlockPosition;

public class PlayerDiggingListener {

    public static void playerDiggingListener(ClientPlayerDiggingPacket packet, Player player) {
        ClientPlayerDiggingPacket.Status status = packet.status;
        BlockPosition blockPosition = packet.blockPosition;

        PlayerInventory playerInventory = player.getInventory();
        ItemStack mainHand = playerInventory.getItemInMainHand();
        ItemStack offHand = playerInventory.getItemInOffHand();

        Instance instance = player.getInstance();

        if (instance == null)
            return;
        final short blockId = instance.getBlockId(blockPosition);

        switch (status) {
            case STARTED_DIGGING:
                final boolean instantBreak = player.isCreative() ||
                        player.isInstantBreak() ||
                        Block.fromId(blockId).breaksInstantaneously();

                if (instantBreak) {
                    instance.breakBlock(player, blockPosition);

                    if (!player.isCreative()) {
                        if (player.getCustomBlockTarget() != null) {
                            player.resetTargetBlock();
                            removeEffect(player);
                        }

                        sendAcknowledgePacket(player, blockPosition, blockId,
                                ClientPlayerDiggingPacket.Status.FINISHED_DIGGING, true);
                    }
                } else {
                    CustomBlock customBlock = instance.getCustomBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
                    if (customBlock != null) {
                        int breakTime = customBlock.getBreakDelay(player, blockPosition);
                        if (breakTime >= 0) {
                            PlayerStartDiggingEvent playerStartDiggingEvent = new PlayerStartDiggingEvent(blockPosition, customBlock);
                            player.callEvent(PlayerStartDiggingEvent.class, playerStartDiggingEvent);
                            if (!playerStartDiggingEvent.isCancelled()) {
                                player.refreshTargetBlock(customBlock, blockPosition, breakTime);
                                sendAcknowledgePacket(player, blockPosition, customBlock.getBlockId(),
                                        ClientPlayerDiggingPacket.Status.STARTED_DIGGING, true);
                            } else {
                                sendAcknowledgePacket(player, blockPosition, customBlock.getBlockId(),
                                        ClientPlayerDiggingPacket.Status.STARTED_DIGGING, false);
                            }
                            addEffect(player);
                        } else {
                            if (player.getCustomBlockTarget() != null) {
                                player.resetTargetBlock();
                                removeEffect(player);
                            }

                            instance.breakBlock(player, blockPosition);

                            sendAcknowledgePacket(player, blockPosition, customBlock.getBlockId(),
                                    ClientPlayerDiggingPacket.Status.FINISHED_DIGGING, true);
                        }
                    } else {
                        if (player.getCustomBlockTarget() != null) {
                            player.resetTargetBlock();
                            removeEffect(player);
                        }
                    }
                }
                break;
            case CANCELLED_DIGGING:
                player.resetTargetBlock();
                removeEffect(player);

                sendAcknowledgePacket(player, blockPosition, blockId,
                        ClientPlayerDiggingPacket.Status.CANCELLED_DIGGING, true);
                break;
            case FINISHED_DIGGING:
                if (player.getCustomBlockTarget() != null) {
                    player.resetTargetBlock();
                    removeEffect(player);
                } else {
                    if (instance != null) {
                        instance.breakBlock(player, blockPosition);
                    }

                    sendAcknowledgePacket(player, blockPosition, blockId,
                            ClientPlayerDiggingPacket.Status.FINISHED_DIGGING, true);
                }
                break;
            case DROP_ITEM_STACK:
                ItemStack droppedItemStack = player.getInventory().getItemInMainHand().clone();
                dropItem(player, droppedItemStack, ItemStack.getAirItem());
                break;
            case DROP_ITEM:
                ItemStack handItem = player.getInventory().getItemInMainHand().clone();
                ItemStack droppedItemStack2 = handItem.clone();
                StackingRule handStackingRule = handItem.getStackingRule();

                droppedItemStack2 = handStackingRule.apply(droppedItemStack2, 1);

                handItem = handStackingRule.apply(handItem, handStackingRule.getAmount(handItem) - 1);

                dropItem(player, droppedItemStack2, handItem);
                break;
            case UPDATE_ITEM_STATE:
                player.refreshEating(false);
                ItemUpdateStateEvent itemUpdateStateEvent = player.callItemUpdateStateEvent(false);

                if (itemUpdateStateEvent == null) {
                    player.refreshActiveHand(true, false, false);
                } else {
                    boolean isOffHand = itemUpdateStateEvent.getHand() == Player.Hand.OFF;
                    player.refreshActiveHand(itemUpdateStateEvent.hasHandAnimation(), isOffHand, false);
                }

                break;
            case SWAP_ITEM_HAND:
                PlayerSwapItemEvent swapItemEvent = new PlayerSwapItemEvent(offHand.clone(), mainHand.clone());
                player.callCancellableEvent(PlayerSwapItemEvent.class, swapItemEvent, () -> {
                    synchronized (playerInventory) {
                        playerInventory.setItemInMainHand(swapItemEvent.getMainHandItem());
                        playerInventory.setItemInOffHand(swapItemEvent.getOffHandItem());
                    }
                });
                break;
        }
    }

    private static void dropItem(Player player, ItemStack droppedItem, ItemStack handItem) {
        PlayerInventory playerInventory = player.getInventory();
        if (player.dropItem(droppedItem)) {
            playerInventory.setItemInMainHand(handItem);
        } else {
            playerInventory.update();
        }
    }

    private static void addEffect(Player player) {
        EntityEffectPacket entityEffectPacket = new EntityEffectPacket();
        entityEffectPacket.entityId = player.getEntityId();
        entityEffectPacket.effectId = 4;
        entityEffectPacket.amplifier = -1;
        entityEffectPacket.duration = 0;
        entityEffectPacket.flags = 0;
        player.getPlayerConnection().sendPacket(entityEffectPacket);
    }

    private static void removeEffect(Player player) {
        RemoveEntityEffectPacket removeEntityEffectPacket = new RemoveEntityEffectPacket();
        removeEntityEffectPacket.entityId = player.getEntityId();
        removeEntityEffectPacket.effectId = 4;
        player.getPlayerConnection().sendPacket(removeEntityEffectPacket);
    }

    private static void sendAcknowledgePacket(Player player, BlockPosition blockPosition, int blockId,
                                              ClientPlayerDiggingPacket.Status status, boolean success) {
        AcknowledgePlayerDiggingPacket acknowledgePlayerDiggingPacket = new AcknowledgePlayerDiggingPacket();
        acknowledgePlayerDiggingPacket.blockPosition = blockPosition;
        acknowledgePlayerDiggingPacket.blockStateId = blockId;
        acknowledgePlayerDiggingPacket.status = status;
        acknowledgePlayerDiggingPacket.successful = success;

        player.getPlayerConnection().sendPacket(acknowledgePlayerDiggingPacket);
    }

}
