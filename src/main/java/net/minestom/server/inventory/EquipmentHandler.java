package net.minestom.server.inventory;

import net.minestom.server.event.ArmorEquipEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;

import static net.minestom.server.utils.inventory.PlayerInventoryUtils.*;

public interface EquipmentHandler {

    ItemStack getItemInMainHand();

    void setItemInMainHand(ItemStack itemStack);

    ItemStack getItemInOffHand();

    void setItemInOffHand(ItemStack itemStack);

    ItemStack getHelmet();

    void setHelmet(ItemStack itemStack);

    ItemStack getChestplate();

    void setChestplate(ItemStack itemStack);

    ItemStack getLeggings();

    void setLeggings(ItemStack itemStack);

    ItemStack getBoots();

    void setBoots(ItemStack itemStack);

    default ItemStack getEquipment(EntityEquipmentPacket.Slot slot) {
        switch (slot) {
            case MAIN_HAND:
                return getItemInMainHand();
            case OFF_HAND:
                return getItemInOffHand();
            case HELMET:
                return getHelmet();
            case CHESTPLATE:
                return getChestplate();
            case LEGGINGS:
                return getLeggings();
            case BOOTS:
                return getBoots();
            default:
                throw new NullPointerException("Equipment slot cannot be null");
        }
    }

    default ArmorEquipEvent getArmorEquipEventPacket(int slot, ItemStack itemStack) {
        if (slot == HELMET_SLOT) {
            return new ArmorEquipEvent(itemStack, ArmorEquipEvent.ArmorSlot.HELMET);
        } else if (slot == CHESTPLATE_SLOT) {
            return new ArmorEquipEvent(itemStack, ArmorEquipEvent.ArmorSlot.CHESTPLATE);
        } else if (slot == LEGGINGS_SLOT) {
            return new ArmorEquipEvent(itemStack, ArmorEquipEvent.ArmorSlot.LEGGINGS);
        } else if (slot == BOOTS_SLOT) {
            return new ArmorEquipEvent(itemStack, ArmorEquipEvent.ArmorSlot.BOOTS);
        }

        return null;
    }

}
