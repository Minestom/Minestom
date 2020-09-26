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
import net.minestom.server.potion.PotionType;
import net.minestom.server.utils.BlockPosition;

public class PlayerDiggingListener {

    public static void playerDiggingListener(ClientPlayerDiggingPacket packet, Player player) {
        final ClientPlayerDiggingPacket.Status status = packet.status;
        final BlockPosition blockPosition = packet.blockPosition;

        final PlayerInventory playerInventory = player.getInventory();
        final ItemStack mainHand = playerInventory.getItemInMainHand();
        final ItemStack offHand = playerInventory.getItemInOffHand();

        final Instance instance = player.getInstance();

        if (instance == null)
            return;

        final short blockStateId = instance.getBlockStateId(blockPosition);

        switch (status) {
            case STARTED_DIGGING:
                final boolean instantBreak = player.isCreative() ||
                        player.isInstantBreak() ||
                        Block.fromStateId(blockStateId).breaksInstantaneously();

                if (instantBreak) {
                    breakBlock(instance, player, blockPosition, blockStateId);
                } else {
                    final CustomBlock customBlock = instance.getCustomBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
                    if (customBlock != null) {
                        // Custom block has a custom break time, allow for digging event
                        PlayerStartDiggingEvent playerStartDiggingEvent = new PlayerStartDiggingEvent(player, blockPosition, customBlock);
                        player.callEvent(PlayerStartDiggingEvent.class, playerStartDiggingEvent);
                        if (!playerStartDiggingEvent.isCancelled()) {

                            // Start digging the block
                            if (customBlock.enableCustomBreakDelay()) {
                                customBlock.startDigging(instance, blockPosition, player);
                                addEffect(player);
                            }

                            sendAcknowledgePacket(player, blockPosition, customBlock.getDefaultBlockStateId(),
                                    ClientPlayerDiggingPacket.Status.STARTED_DIGGING, true);
                        } else {
                            // Unsuccessful digging
                            sendAcknowledgePacket(player, blockPosition, customBlock.getDefaultBlockStateId(),
                                    ClientPlayerDiggingPacket.Status.STARTED_DIGGING, false);
                        }
                    } else {
                        // Player is not mining a custom block, be sure that he doesn't have the effect
                        player.resetTargetBlock();
                    }
                }
                break;
            case CANCELLED_DIGGING:
                // Remove custom block target
                player.resetTargetBlock();

                sendAcknowledgePacket(player, blockPosition, blockStateId,
                        ClientPlayerDiggingPacket.Status.CANCELLED_DIGGING, true);
                break;
            case FINISHED_DIGGING:
                breakBlock(instance, player, blockPosition, blockStateId);
                break;
            case DROP_ITEM_STACK:
                final ItemStack droppedItemStack = player.getInventory().getItemInMainHand().clone();
                dropItem(player, droppedItemStack, ItemStack.getAirItem());
                break;
            case DROP_ITEM:
                ItemStack handItem = player.getInventory().getItemInMainHand().clone();
                ItemStack droppedItemStack2 = handItem.clone();
                final StackingRule handStackingRule = handItem.getStackingRule();

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
                    final boolean isOffHand = itemUpdateStateEvent.getHand() == Player.Hand.OFF;
                    player.refreshActiveHand(itemUpdateStateEvent.hasHandAnimation(), isOffHand, false);
                }

                break;
            case SWAP_ITEM_HAND:
                PlayerSwapItemEvent swapItemEvent = new PlayerSwapItemEvent(player, offHand.clone(), mainHand.clone());
                player.callCancellableEvent(PlayerSwapItemEvent.class, swapItemEvent, () -> {
                    synchronized (playerInventory) {
                        playerInventory.setItemInMainHand(swapItemEvent.getMainHandItem());
                        playerInventory.setItemInOffHand(swapItemEvent.getOffHandItem());
                    }
                });
                break;
        }
    }

    private static void breakBlock(Instance instance, Player player, BlockPosition blockPosition, int blockStateId) {
        // Finished digging, remove effect if any
        player.resetTargetBlock();

        // Unverified block break, client is fully responsive
        final boolean result = instance.breakBlock(player, blockPosition);

        final int updatedBlockId = result ? 0 : blockStateId;
        final ClientPlayerDiggingPacket.Status status = result ?
                ClientPlayerDiggingPacket.Status.FINISHED_DIGGING :
                ClientPlayerDiggingPacket.Status.CANCELLED_DIGGING;

        // Send acknowledge packet to allow or cancel the digging process
        sendAcknowledgePacket(player, blockPosition, updatedBlockId,
                status, result);

        if (!result) {
            final boolean solid = Block.fromStateId((short) blockStateId).isSolid();
            if (solid) {
                final BlockPosition playerBlockPosition = player.getPosition().toBlockPosition();

                // Teleport the player back if he broke a solid block just below him
                if (playerBlockPosition.subtract(0, 1, 0).equals(blockPosition))
                    player.teleport(player.getPosition());
            }
        }
    }

    private static void dropItem(Player player, ItemStack droppedItem, ItemStack handItem) {
        final PlayerInventory playerInventory = player.getInventory();
        if (player.dropItem(droppedItem)) {
            playerInventory.setItemInMainHand(handItem);
        } else {
            playerInventory.update();
        }
    }

    private static void addEffect(Player player) {
        EntityEffectPacket entityEffectPacket = new EntityEffectPacket();
        entityEffectPacket.entityId = player.getEntityId();
        entityEffectPacket.effect = PotionType.AWKWARD;
        entityEffectPacket.amplifier = -1;
        entityEffectPacket.duration = 0;
        entityEffectPacket.flags = 0;
        player.getPlayerConnection().sendPacket(entityEffectPacket);
    }

    private static void sendAcknowledgePacket(Player player, BlockPosition blockPosition, int blockStateId,
                                              ClientPlayerDiggingPacket.Status status, boolean success) {
        AcknowledgePlayerDiggingPacket acknowledgePlayerDiggingPacket = new AcknowledgePlayerDiggingPacket();
        acknowledgePlayerDiggingPacket.blockPosition = blockPosition;
        acknowledgePlayerDiggingPacket.blockStateId = blockStateId;
        acknowledgePlayerDiggingPacket.status = status;
        acknowledgePlayerDiggingPacket.successful = success;

        player.getPlayerConnection().sendPacket(acknowledgePlayerDiggingPacket);
    }

}
