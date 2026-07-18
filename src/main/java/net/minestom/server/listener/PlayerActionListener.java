package net.minestom.server.listener;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.PlayerCancelItemUseEvent;
import net.minestom.server.event.player.PlayerStabEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockActions;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket;

public final class PlayerActionListener {

    public static void playerActionListener(ClientPlayerActionPacket packet, Player player) {
        final ClientPlayerActionPacket.Status status = packet.status();
        final Instance instance = player.getInstance();
        if (instance == null) return;

        switch (status) {
            case STARTED_DIGGING, CANCELLED_DIGGING, FINISHED_DIGGING ->
                    BlockActions.dig(BlockActions.world(instance), player, packet);
            case DROP_ITEM_STACK -> dropStack(player);
            case DROP_ITEM -> dropSingle(player);
            case UPDATE_ITEM_STATE -> updateItemState(player);
            case SWAP_ITEM_HAND -> swapItemHand(player);
            case STAB -> stab(player);
        }
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
        player.refreshActiveHand(false, isOffHand, cancelUseEvent.isRiptideSpinAttack());
        player.clearItemUse();
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
}
