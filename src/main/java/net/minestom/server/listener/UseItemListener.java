package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.animation.ArmAnimationEvent;
import net.minestom.server.event.item.ArmorEquipEvent;
import net.minestom.server.event.player.PlayerPreEatEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientUseItemPacket;

public class UseItemListener {

    public static void useItemListener(ClientUseItemPacket packet, Player player) {
        PlayerInventory inventory = player.getInventory();
        Player.Hand hand = packet.hand;
        ItemStack itemStack = hand == Player.Hand.MAIN ? inventory.getItemInMainHand() : inventory.getItemInOffHand();
        PlayerUseItemEvent useItemEvent = new PlayerUseItemEvent(hand, itemStack);
        player.callEvent(PlayerUseItemEvent.class, useItemEvent);

        Material material = Material.fromId(itemStack.getMaterialId());

        if (material.isArmor()) {
            PlayerInventory playerInventory = player.getInventory();
            if (useItemEvent.isCancelled()) {
                playerInventory.update();
                return;
            }

            ArmorEquipEvent.ArmorSlot armorSlot;
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
            ItemStack armorItem = armorEquipEvent.getArmorItem();

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

        ArmAnimationEvent armAnimationEvent = null;
        boolean offhand = hand == Player.Hand.OFF;
        boolean riptideSpinAttack = false;

        if (material == Material.BOW) {
            armAnimationEvent = new ArmAnimationEvent(ArmAnimationEvent.ArmAnimationType.BOW);
        } else if (material == Material.CROSSBOW) {
            armAnimationEvent = new ArmAnimationEvent(ArmAnimationEvent.ArmAnimationType.CROSSBOW);
        } else if (material == Material.SHIELD) {
            armAnimationEvent = new ArmAnimationEvent(ArmAnimationEvent.ArmAnimationType.SHIELD);
        } else if (material == Material.TRIDENT) {
            armAnimationEvent = new ArmAnimationEvent(ArmAnimationEvent.ArmAnimationType.TRIDENT);
        } else if (material.isFood()) {
            armAnimationEvent = new ArmAnimationEvent(ArmAnimationEvent.ArmAnimationType.EAT);

            // Eating code, contains the eating time customisation
            PlayerPreEatEvent playerPreEatEvent = new PlayerPreEatEvent(player, itemStack, player.getDefaultEatingTime());
            player.callCancellableEvent(PlayerPreEatEvent.class, playerPreEatEvent, () -> {
                player.refreshEating(true, playerPreEatEvent.getEatingTime());
            });
        }

        if (armAnimationEvent != null)
            player.callCancellableEvent(ArmAnimationEvent.class, armAnimationEvent, () -> {
                player.refreshActiveHand(true, offhand, riptideSpinAttack);
                player.sendPacketToViewers(player.getMetadataPacket());
            });
    }

}
