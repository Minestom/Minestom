package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerItemAnimationEvent;
import net.minestom.server.event.player.PlayerPreEatEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.Consumable;
import net.minestom.server.item.component.Equippable;
import net.minestom.server.network.packet.client.play.ClientUseItemPacket;
import net.minestom.server.network.packet.server.play.AcknowledgeBlockChangePacket;
import org.jetbrains.annotations.NotNull;

public class UseItemListener {

    public static void useItemListener(ClientUseItemPacket packet, Player player) {
        final PlayerHand hand = packet.hand();
        final ItemStack itemStack = player.getInventory().getItemInHand(hand);
        final Material material = itemStack.material();

        PlayerUseItemEvent useItemEvent = new PlayerUseItemEvent(player, hand, itemStack, defaultUseItemTime(itemStack));
        EventDispatcher.call(useItemEvent);

        player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
        final PlayerInventory playerInventory = player.getInventory();
        if (useItemEvent.isCancelled()) {
            playerInventory.update();
            return;
        }

        // Equip armor with right click
        final Equippable equippable = itemStack.get(ItemComponent.EQUIPPABLE);
        if (equippable != null && equippable.swappable()) {
            final ItemStack currentlyEquipped = player.getEquipment(equippable.slot());
            player.setEquipment(equippable.slot(), itemStack);
            player.setItemInHand(hand, currentlyEquipped);
        }

        long itemUseTime = useItemEvent.getItemUseTime();
        PlayerItemAnimationEvent.ItemAnimationType itemAnimationType;

        if (material == Material.BOW) {
            itemAnimationType = PlayerItemAnimationEvent.ItemAnimationType.BOW;
        } else if (material == Material.CROSSBOW) {
            itemAnimationType = PlayerItemAnimationEvent.ItemAnimationType.CROSSBOW;
        } else if (material == Material.SHIELD) {
            itemAnimationType = PlayerItemAnimationEvent.ItemAnimationType.SHIELD;
        } else if (material == Material.TRIDENT) {
            itemAnimationType = PlayerItemAnimationEvent.ItemAnimationType.TRIDENT;
        } else if (material == Material.SPYGLASS) {
            itemAnimationType = PlayerItemAnimationEvent.ItemAnimationType.SPYGLASS;
        } else if (material == Material.GOAT_HORN) {
            itemAnimationType = PlayerItemAnimationEvent.ItemAnimationType.HORN;
        } else if (material == Material.BRUSH) {
            itemAnimationType = PlayerItemAnimationEvent.ItemAnimationType.BRUSH;
        } else if (itemStack.has(ItemComponent.FOOD) || itemStack.material() == Material.POTION) {
            itemAnimationType = PlayerItemAnimationEvent.ItemAnimationType.EAT;

            PlayerPreEatEvent playerPreEatEvent = new PlayerPreEatEvent(player, itemStack, hand, itemUseTime);
            EventDispatcher.call(playerPreEatEvent);
            if (playerPreEatEvent.isCancelled()) return;
            itemUseTime = playerPreEatEvent.getEatingTime();
        } else {
            itemAnimationType = PlayerItemAnimationEvent.ItemAnimationType.OTHER;
        }

        if (itemUseTime != 0) {
            player.refreshItemUse(hand, itemUseTime);

            PlayerItemAnimationEvent playerItemAnimationEvent = new PlayerItemAnimationEvent(player, itemAnimationType, hand);
            EventDispatcher.callCancellable(playerItemAnimationEvent, () -> {
                player.refreshActiveHand(true, hand == PlayerHand.OFF, false);
                player.sendPacketToViewers(player.getMetadataPacket());
            });
        }
    }

    private static int defaultUseItemTime(@NotNull ItemStack itemStack) {
        final Consumable consumable = itemStack.get(ItemComponent.CONSUMABLE);
        return consumable != null ? consumable.consumeTicks() : 0;
    }
}
