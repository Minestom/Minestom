package fr.themode.minestom.listener;

import fr.themode.minestom.entity.GameMode;
import fr.themode.minestom.entity.ItemEntity;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.ItemDropEvent;
import fr.themode.minestom.event.PlayerStartDiggingEvent;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.block.CustomBlock;
import fr.themode.minestom.inventory.PlayerInventory;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.client.play.ClientPlayerDiggingPacket;
import fr.themode.minestom.net.packet.server.play.EntityEffectPacket;
import fr.themode.minestom.net.packet.server.play.RemoveEntityEffectPacket;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Vector;

public class PlayerDiggingListener {

    public static void playerDiggingListener(ClientPlayerDiggingPacket packet, Player player) {
        ClientPlayerDiggingPacket.Status status = packet.status;
        BlockPosition blockPosition = packet.blockPosition;
        switch (status) {
            case STARTED_DIGGING:
                if (player.getGameMode() == GameMode.CREATIVE) {
                    Instance instance = player.getInstance();
                    if (instance != null) {
                        instance.breakBlock(player, blockPosition);
                    }
                } else if (player.getGameMode() == GameMode.SURVIVAL) {
                    Instance instance = player.getInstance();
                    if (instance != null) {
                        CustomBlock customBlock = instance.getCustomBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
                        if (customBlock != null) {
                            PlayerStartDiggingEvent playerStartDiggingEvent = new PlayerStartDiggingEvent(blockPosition, customBlock);
                            player.callEvent(PlayerStartDiggingEvent.class, playerStartDiggingEvent);
                            if (!playerStartDiggingEvent.isCancelled()) {
                                player.refreshTargetBlock(customBlock, blockPosition);
                                // TODO ACKNOWLEDGE
                            }
                            addEffect(player);
                        } else {
                            player.resetTargetBlock();
                            removeEffect(player);
                            // TODO ACKNOWLEDGE
                        }
                    }
                }
                break;
            case CANCELLED_DIGGING:
                player.resetTargetBlock();
                removeEffect(player);
                break;
            case FINISHED_DIGGING:
                if (player.getCustomBlockTarget() != null) {
                    player.resetTargetBlock();
                    removeEffect(player);
                } else {
                    Instance instance = player.getInstance();
                    if (instance != null) {
                        instance.breakBlock(player, blockPosition);
                    }
                }
                break;
            case DROP_ITEM_STACK:
                ItemStack droppedItemStack = player.getInventory().getItemInMainHand().clone();
                dropItem(player, droppedItemStack, ItemStack.AIR_ITEM);
                break;
            case DROP_ITEM:
                ItemStack droppedItemStack2 = player.getInventory().getItemInMainHand().clone();
                droppedItemStack2.setAmount((byte) 1);

                ItemStack handItem = player.getInventory().getItemInMainHand();
                handItem.setAmount((byte) (handItem.getAmount() - 1));
                handItem = handItem.getAmount() <= 0 ? ItemStack.AIR_ITEM : handItem;
                dropItem(player, droppedItemStack2, handItem);
                break;
            case UPDATE_ITEM_STATE:
                // TODO check if is updatable item
                //player.refreshActiveHand(false, false, false);
                //player.sendPacketToViewers(player.getMetadataPacket());
                break;
            case SWAP_ITEM_HAND:
                PlayerInventory playerInventory = player.getInventory();
                ItemStack mainHand = playerInventory.getItemInMainHand().clone();
                ItemStack offHand = playerInventory.getItemInOffHand().clone();
                playerInventory.setItemInMainHand(offHand);
                playerInventory.setItemInOffHand(mainHand);
                break;
        }
    }

    private static void dropItem(Player player, ItemStack droppedItem, ItemStack handItem) {
        ItemDropEvent itemDropEvent = new ItemDropEvent(droppedItem);
        player.callCancellableEvent(ItemDropEvent.class, itemDropEvent, () -> {
            player.getInventory().setItemInMainHand(handItem);

            ItemEntity itemEntity = new ItemEntity(droppedItem);
            itemEntity.setPickupDelay(500);
            itemEntity.refreshPosition(player.getPosition().clone().add(0, 1.5f, 0));
            itemEntity.setInstance(player.getInstance());
            Vector velocity = player.getPosition().clone().getDirection().multiply(5);
            itemEntity.setVelocity(velocity, 350);
        });
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

}
