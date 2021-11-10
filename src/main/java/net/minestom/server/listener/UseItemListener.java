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

public class UseItemListener {

    public static void useItemListener(ClientUseItemPacket packet, Player player) {
        final PlayerInventory inventory = player.getInventory();
        final Player.Hand hand = packet.hand;
        ItemStack itemStack = hand == Player.Hand.MAIN ? inventory.getItemInMainHand() : inventory.getItemInOffHand();
        //itemStack.onRightClick(player, hand);
        PlayerUseItemEvent useItemEvent = new PlayerUseItemEvent(player, hand, itemStack);
        EventDispatcher.call(useItemEvent);

        final PlayerInventory playerInventory = player.getInventory();
        if (useItemEvent.isCancelled()) {
            playerInventory.update();
            return;
        }

        itemStack = useItemEvent.getItemStack();
        final Material material = itemStack.getMaterial();

        // Equip armor with right click
        final EquipmentSlot equipmentSlot = material.registry().equipmentSlot();
        if (equipmentSlot != null) {
            final ItemStack currentlyEquipped = playerInventory.getEquipment(equipmentSlot);
            if (currentlyEquipped.isAir()) {
                playerInventory.setEquipment(equipmentSlot, itemStack);
                playerInventory.setItemInHand(hand, currentlyEquipped);
            }
        }

        PlayerItemAnimationEvent.ItemAnimationType itemAnimationType = null;
        boolean riptideSpinAttack = false;

        boolean cancelAnimation = false;

        if (material == Material.BOW) {
            itemAnimationType = PlayerItemAnimationEvent.ItemAnimationType.BOW;
        } else if (material == Material.CROSSBOW) {
            itemAnimationType = PlayerItemAnimationEvent.ItemAnimationType.CROSSBOW;
        } else if (material == Material.SHIELD) {
            itemAnimationType = PlayerItemAnimationEvent.ItemAnimationType.SHIELD;
        } else if (material == Material.TRIDENT) {
            itemAnimationType = PlayerItemAnimationEvent.ItemAnimationType.TRIDENT;
        } else if (material.isFood()) {
            itemAnimationType = PlayerItemAnimationEvent.ItemAnimationType.EAT;

            // Eating code, contains the eating time customisation
            PlayerPreEatEvent playerPreEatEvent = new PlayerPreEatEvent(player, itemStack, hand, player.getDefaultEatingTime());
            EventDispatcher.callCancellable(playerPreEatEvent, () -> player.refreshEating(hand, playerPreEatEvent.getEatingTime()));

            if (playerPreEatEvent.isCancelled()) {
                cancelAnimation = true;
            }
        }

        if (!cancelAnimation && itemAnimationType != null) {
            PlayerItemAnimationEvent playerItemAnimationEvent = new PlayerItemAnimationEvent(player, itemAnimationType);
            EventDispatcher.callCancellable(playerItemAnimationEvent, () -> {
                player.refreshActiveHand(true, hand == Player.Hand.OFF, riptideSpinAttack);
                player.sendPacketToViewers(player.getMetadataPacket());
            });
        }
    }

}
