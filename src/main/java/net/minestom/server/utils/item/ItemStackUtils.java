package net.minestom.server.utils.item;

import net.minestom.server.item.ItemStack;

public final class ItemStackUtils {

    private ItemStackUtils() {

    }

    /**
     * Ensure that the returned ItemStack won't be null
     * by replacing every null instance by a new Air one
     *
     * @param itemStack the ItemStack to return if not null
     * @return {@code itemStack} if not null, {@link ItemStack#getAirItem()} otherwise
     */
    public static ItemStack notNull(ItemStack itemStack) {
        return itemStack == null ? ItemStack.getAirItem() : itemStack;
    }

    /**
     * Used to check if the item stack is a visible item (not null and not air)
     *
     * @param itemStack the item to check
     * @return true if the item is visible, false otherwise
     */
    public static boolean isVisible(ItemStack itemStack) {
        return itemStack != null && !itemStack.isAir();
    }
}
