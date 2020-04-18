package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.ArmorEquipEvent;
import fr.themode.minestom.event.PlayerUseItemEvent;
import fr.themode.minestom.inventory.PlayerInventory;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.item.Material;
import fr.themode.minestom.net.packet.client.play.ClientUseItemPacket;

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
            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(itemStack, armorSlot);
            player.callEvent(ArmorEquipEvent.class, armorEquipEvent);
            ItemStack armorItem = armorEquipEvent.getArmorItem();

            if (hand == Player.Hand.MAIN) {
                playerInventory.setItemInMainHand(ItemStack.AIR_ITEM);
            } else {
                playerInventory.setItemInOffHand(ItemStack.AIR_ITEM);
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

        // TODO check if item in main or off hand is food or item with animation (bow/crossbow/riptide)
        // TODO in material enum?
        //player.refreshActiveHand(true, false, false);
        //player.sendPacketToViewers(player.getMetadataPacket());
    }

}
