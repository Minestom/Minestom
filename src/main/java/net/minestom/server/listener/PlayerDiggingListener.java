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
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.server.play.AcknowledgePlayerDiggingPacket;
import net.minestom.server.network.packet.server.play.EntityEffectPacket;
import net.minestom.server.network.packet.server.play.RemoveEntityEffectPacket;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerDiggingListener {

    private static final List<Player> playersEffect = new CopyOnWriteArrayList<>();

    public static void playerDiggingListener(ClientPlayerDiggingPacket packet, Player player) {
        final ClientPlayerDiggingPacket.Status status = packet.status;
        final BlockPosition blockPosition = packet.blockPosition;

        final Instance instance = player.getInstance();

        if (instance == null)
            return;

        if (status == ClientPlayerDiggingPacket.Status.STARTED_DIGGING) {
            final short blockStateId = instance.getBlockStateId(blockPosition);

            //Check if the player is allowed to break blocks based on their game mode
            if (player.getGameMode() == GameMode.SPECTATOR) {
                sendAcknowledgePacket(player, blockPosition, blockStateId,
                        ClientPlayerDiggingPacket.Status.STARTED_DIGGING, false);
                return; //Spectators can't break blocks
            } else if (player.getGameMode() == GameMode.ADVENTURE) {
                //Check if the item can break the block with the current item
                ItemStack itemInMainHand = player.getItemInMainHand();
                Block destroyedBlock = instance.getBlock(blockPosition);
                if (!itemInMainHand.getMeta().getCanDestroy().contains(destroyedBlock)) {
                    sendAcknowledgePacket(player, blockPosition, blockStateId,
                            ClientPlayerDiggingPacket.Status.STARTED_DIGGING, false);
                    return;
                }
            }

            final boolean instantBreak = player.isCreative() ||
                    player.isInstantBreak() ||
                    Block.fromStateId(blockStateId).breaksInstantaneously();

            if (instantBreak) {
                // No need to check custom block
                breakBlock(instance, player, blockPosition, blockStateId, status);
            } else {
                final CustomBlock customBlock = instance.getCustomBlock(blockPosition);
                final int customBlockId = customBlock == null ? 0 : customBlock.getCustomBlockId();

                PlayerStartDiggingEvent playerStartDiggingEvent = new PlayerStartDiggingEvent(player, blockPosition, blockStateId, customBlockId);
                EventDispatcher.call(playerStartDiggingEvent);

                if (playerStartDiggingEvent.isCancelled()) {
                    addEffect(player);

                    // Unsuccessful digging
                    sendAcknowledgePacket(player, blockPosition, blockStateId,
                            ClientPlayerDiggingPacket.Status.STARTED_DIGGING, false);
                } else if (customBlock != null) {
                    // Start digging the custom block
                    if (customBlock.enableCustomBreakDelay()) {
                        customBlock.startDigging(instance, blockPosition, player);
                        addEffect(player);
                    }

                    sendAcknowledgePacket(player, blockPosition, blockStateId,
                            ClientPlayerDiggingPacket.Status.STARTED_DIGGING, true);
                }
            }

        } else if (status == ClientPlayerDiggingPacket.Status.CANCELLED_DIGGING) {

            final short blockStateId = instance.getBlockStateId(blockPosition);
            // Remove custom block target
            player.resetTargetBlock();

            sendAcknowledgePacket(player, blockPosition, blockStateId,
                    ClientPlayerDiggingPacket.Status.CANCELLED_DIGGING, true);

        } else if (status == ClientPlayerDiggingPacket.Status.FINISHED_DIGGING) {

            final short blockStateId = instance.getBlockStateId(blockPosition);
            final CustomBlock customBlock = instance.getCustomBlock(blockPosition);
            if (customBlock != null && customBlock.enableCustomBreakDelay()) {
                // Is not supposed to happen, probably a bug
                sendAcknowledgePacket(player, blockPosition, blockStateId,
                        ClientPlayerDiggingPacket.Status.FINISHED_DIGGING, false);
            } else {
                // Vanilla block
                breakBlock(instance, player, blockPosition, blockStateId, status);
            }

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
                                   BlockPosition blockPosition, int blockStateId,
                                   ClientPlayerDiggingPacket.Status status) {
        // Finished digging, remove effect if any
        player.resetTargetBlock();

        // Unverified block break, client is fully responsible
        final boolean result = instance.breakBlock(player, blockPosition);

        final int updatedBlockId = instance.getBlockStateId(blockPosition);

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
     * Adds the effect {@link PotionEffect#DIG_SLOWDOWN} to the player.
     * <p>
     * Used for {@link CustomBlock} break delay or when the {@link PlayerStartDiggingEvent} is cancelled
     * to remove the player break animation.
     *
     * @param player the player to add the effect to
     */
    private static void addEffect(@NotNull Player player) {
        playersEffect.add(player);

        EntityEffectPacket entityEffectPacket = new EntityEffectPacket();
        entityEffectPacket.entityId = player.getEntityId();
        entityEffectPacket.potion = new Potion(
                PotionEffect.DIG_SLOWDOWN,
                (byte) -1,
                0,
                false,
                false,
                false
        );
        player.getPlayerConnection().sendPacket(entityEffectPacket);
    }

    /**
     * Used to remove the affect from {@link #addEffect(Player)}.
     * <p>
     * Called when the player cancelled or finished digging the {@link CustomBlock}.
     *
     * @param player the player to remove the effect to
     */
    public static void removeEffect(@NotNull Player player) {
        if (playersEffect.contains(player)) {
            playersEffect.remove(player);

            RemoveEntityEffectPacket removeEntityEffectPacket = new RemoveEntityEffectPacket();
            removeEntityEffectPacket.entityId = player.getEntityId();
            removeEntityEffectPacket.effect = PotionEffect.DIG_SLOWDOWN;
            player.getPlayerConnection().sendPacket(removeEntityEffectPacket);
        }
    }

    /**
     * Sends an {@link AcknowledgePlayerDiggingPacket} to a connection.
     *
     * @param player        the player
     * @param blockPosition the block position
     * @param blockStateId  the block state id
     * @param status        the status of the digging
     * @param success       true to notify of a success, false otherwise
     */
    private static void sendAcknowledgePacket(@NotNull Player player, @NotNull BlockPosition blockPosition, int blockStateId,
                                              @NotNull ClientPlayerDiggingPacket.Status status, boolean success) {
        player.getPlayerConnection().sendPacket(new AcknowledgePlayerDiggingPacket(blockPosition, blockStateId, status, success));
    }
}
