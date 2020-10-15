package net.minestom.server.item;

import net.minestom.server.chat.ColoredText;

public class ItemDisplay {

    private ColoredText displayName;
    private ColoredText[] lore;

    public ItemDisplay(ColoredText displayName, ColoredText[] lore) {
        this.displayName = displayName;
        this.lore = lore;
    }

    /**
     * Gets the item display name.
     *
     * @return the item display name
     */
    public ColoredText getDisplayName() {
        return displayName;
    }

    /**
     * Gets the item lore.
     *
     * @return the item lore
     */
    public ColoredText[] getLore() {
        return lore;
    }
}
