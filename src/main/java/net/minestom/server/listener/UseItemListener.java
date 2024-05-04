package net.minestom.server.listener;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerItemAnimationEvent;
import net.minestom.server.event.player.PlayerPreEatEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientUseItemPacket;
import net.minestom.server.network.packet.server.play.AcknowledgeBlockChangePacket;

public class UseItemListener {

    public static void useItemListener(ClientUseItemPacket packet, Player player) {
        final PlayerInventory inventory = player.getInventory();
        final Player.Hand hand = packet.hand();
        ItemStack itemStack = hand == Player.Hand.MAIN ? inventory.getItemInMainHand() : inventory.getItemInOffHand();
        final Material material = itemStack.material();

        PlayerUseItemEvent useItemEvent = new PlayerUseItemEvent(player, hand, itemStack, material.isFood() ? player.getDefaultEatingTime() : 0);
        EventDispatcher.call(useItemEvent);

        player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
        final PlayerInventory playerInventory = player.getInventory();
        if (useItemEvent.isCancelled()) {
            playerInventory.update();
            return;
        }

        // Equip armor with right click
        final EquipmentSlot equipmentSlot = material.registry().equipmentSlot();
        if (equipmentSlot != null) {
            final ItemStack currentlyEquipped = playerInventory.getEquipment(equipmentSlot);
            if (currentlyEquipped.isAir()) {
                playerInventory.setEquipment(equipmentSlot, itemStack);
                playerInventory.setItemInHand(hand, currentlyEquipped);
            }
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
        } else if (material.isFood()) {
            itemAnimationType = PlayerItemAnimationEvent.ItemAnimationType.EAT;

            // Eating code, contains the eating time customisation
            PlayerPreEatEvent playerPreEatEvent = new PlayerPreEatEvent(player, itemStack, hand, itemUseTime);
            EventDispatcher.call(playerPreEatEvent);
            if (playerPreEatEvent.isCancelled()) return;
            itemUseTime = playerPreEatEvent.getEatingTime();
        } else {
            itemAnimationType = PlayerItemAnimationEvent.ItemAnimationType.OTHER;
        }

        if (itemUseTime > 0)
            player.refreshItemUse(hand, itemUseTime);

        PlayerItemAnimationEvent playerItemAnimationEvent = new PlayerItemAnimationEvent(player, itemAnimationType, hand);
        EventDispatcher.callCancellable(playerItemAnimationEvent, () -> {
            player.refreshActiveHand(true, hand == Player.Hand.OFF, false);
            player.sendPacketToViewers(player.getMetadataPacket());
        });
    }
}
