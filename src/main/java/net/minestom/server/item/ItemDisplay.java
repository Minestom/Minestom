package net.minestom.server.item;

import net.minestom.server.chat.JsonMessage;

public class ItemDisplay {

    private JsonMessage displayName;
    private JsonMessage[] lore;

    public ItemDisplay(JsonMessage displayName, JsonMessage[] lore) {
        this.displayName = displayName;
        this.lore = lore;
    }

    /**
     * Gets the item display name.
     *
     * @return the item display name
     */
    public JsonMessage getDisplayName() {
        return displayName;
    }

    /**
     * Gets the item lore.
     *
     * @return the item lore
     */
    public JsonMessage[] getLore() {
        return lore;
    }
}
