package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.item.ArmorEquipEvent;
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
        final ItemStack itemStack = hand == Player.Hand.MAIN ? inventory.getItemInMainHand() : inventory.getItemInOffHand();
        itemStack.onRightClick(player, hand);
        PlayerUseItemEvent useItemEvent = new PlayerUseItemEvent(player, hand, itemStack);
        player.callEvent(PlayerUseItemEvent.class, useItemEvent);

        final Material material = itemStack.getMaterial();

        // Equip armor with right click
        if (material.isArmor()) {
            final PlayerInventory playerInventory = player.getInventory();
            if (useItemEvent.isCancelled()) {
                playerInventory.update();
                return;
            }

            final ArmorEquipEvent.ArmorSlot armorSlot;
            if (material.isHelmet()) {
                armorSlot = ArmorEquipEvent.ArmorSlot.HELMET;
            } else if (material.isChestplate()) {
                armorSlot = ArmorEquipEvent.ArmorSlot.CHESTPLATE;
            } else if (material.isLeggings()) {
                armorSlot = ArmorEquipEvent.ArmorSlot.LEGGINGS;
            } else {
                armorSlot = ArmorEquipEvent.ArmorSlot.BOOTS;
            }
            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, itemStack, armorSlot);
            player.callEvent(ArmorEquipEvent.class, armorEquipEvent);
            final ItemStack armorItem = armorEquipEvent.getArmorItem();

            if (hand == Player.Hand.MAIN) {
                playerInventory.setItemInMainHand(ItemStack.getAirItem());
            } else {
                playerInventory.setItemInOffHand(ItemStack.getAirItem());
            }

            switch (armorSlot) {
                case HELMET:
                    playerInventory.setHelmet(armorItem);
                    break;
                case CHESTPLATE:
                    playerInventory.setChestplate(armorItem);
                    break;
                case LEGGINGS:
                    playerInventory.setLeggings(armorItem);
                    break;
                case BOOTS:
                    playerInventory.setBoots(armorItem);
                    break;
            }
        }

        PlayerItemAnimationEvent.ItemAnimationType itemAnimationType = null;
        final boolean offhand = hand == Player.Hand.OFF;
        boolean riptideSpinAttack = false;

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
            PlayerPreEatEvent playerPreEatEvent = new PlayerPreEatEvent(player, itemStack, player.getDefaultEatingTime());
            player.callCancellableEvent(PlayerPreEatEvent.class, playerPreEatEvent, () -> player.refreshEating(true, playerPreEatEvent.getEatingTime()));
        }

        if (itemAnimationType != null) {
            PlayerItemAnimationEvent playerItemAnimationEvent = new PlayerItemAnimationEvent(player, itemAnimationType);
            player.callCancellableEvent(PlayerItemAnimationEvent.class, playerItemAnimationEvent, () -> {
                player.refreshActiveHand(true, offhand, riptideSpinAttack);
                player.sendPacketToViewers(player.getMetadataPacket());
            });
        }
    }

}
