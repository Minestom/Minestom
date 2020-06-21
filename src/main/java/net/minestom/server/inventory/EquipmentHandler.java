package net.minestom.server.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;

/**
 * Represent an entity which can have item in hand and armor slots
 */
public interface EquipmentHandler {

    /**
     * Get the item in main hand
     *
     * @return the item in main hand
     */
    ItemStack getItemInMainHand();

    /**
     * Change the main hand item
     *
     * @param itemStack the main hand item
     */
    void setItemInMainHand(ItemStack itemStack);

    /**
     * Get the item in off hand
     *
     * @return the item in off hand
     */
    ItemStack getItemInOffHand();

    /**
     * Change the off hand item
     *
     * @param itemStack the off hand item
     */
    void setItemInOffHand(ItemStack itemStack);

    /**
     * Get the item in the specific hand
     *
     * @param hand the hand to get the item from
     * @return the item in {@code hand}
     */
    default ItemStack getItemInHand(Player.Hand hand) {
        switch (hand) {
            case MAIN:
                return getItemInMainHand();

            case OFF:
                return getItemInOffHand();

            default:
                return null;
        }
    }

    /**
     * Change the item in the specific hand
     *
     * @param hand  the hand to set the item to
     * @param stack the itemstack to set
     */
    default void setItemInHand(Player.Hand hand, ItemStack stack) {
        switch (hand) {
            case MAIN:
                setItemInMainHand(stack);
                break;

            case OFF:
                setItemInOffHand(stack);
                break;
        }
    }

    /**
     * Get the helmet
     *
     * @return the helmet
     */
    ItemStack getHelmet();

    /**
     * Change the helmet
     *
     * @param itemStack the helmet
     */
    void setHelmet(ItemStack itemStack);

    /**
     * Get the chestplate
     *
     * @return the chestplate
     */
    ItemStack getChestplate();

    /**
     * Change the chestplate
     *
     * @param itemStack the chestplate
     */
    void setChestplate(ItemStack itemStack);

    /**
     * Get the leggings
     *
     * @return the leggings
     */
    ItemStack getLeggings();

    /**
     * Change the leggings
     *
     * @param itemStack the leggings
     */
    void setLeggings(ItemStack itemStack);

    /**
     * Get the boots
     *
     * @return the boots
     */
    ItemStack getBoots();

    /**
     * Change the boots
     *
     * @param itemStack the boots
     */
    void setBoots(ItemStack itemStack);

    /**
     * Get the equipment in a specific slot
     *
     * @param slot the equipment to get the item from
     * @return the equipment item
     */
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
                return null;
        }
    }

}
