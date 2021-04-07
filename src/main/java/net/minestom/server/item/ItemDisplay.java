package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.chat.JsonMessage;

public class ItemDisplay {

    private Component displayName;
    private Component[] lore;

    /**
     * @deprecated Use {@link #ItemDisplay(Component, Component[])}
     */
    @Deprecated
    public ItemDisplay(JsonMessage displayName, JsonMessage[] lore) {
        this.displayName = displayName.asComponent();
        this.lore = new Component[lore.length];

        for (int i = 0; i < lore.length; i++) {
            this.lore[i] = lore[i].asComponent();
        }
    }

    public ItemDisplay(Component displayName, Component[] lore) {
        this.displayName = displayName;
        this.lore = lore;
    }

    /**
     * Gets the item display name.
     *
     * @return the item display name
     * @deprecated Use {@link #getDisplayName()}
     */
    @Deprecated
    public JsonMessage getDisplayNameJson() {
        return JsonMessage.fromComponent(displayName);
    }

    /**
     * Gets the item lore.
     *
     * @return the item lore
     * @deprecated Use {@link #getLore()}
     */
    @Deprecated
    public JsonMessage[] getLoreJson() {
        JsonMessage[] loreOld = new JsonMessage[lore.length];
        for (int i = 0; i < lore.length; i++) {
            loreOld[i] = JsonMessage.fromComponent(lore[i]);
        }
        return loreOld;
    }

    /**
     * Gets the item display name.
     *
     * @return the item display name
     */
    public Component getDisplayName() {
        return displayName;
    }

    /**
     * Gets the item lore.
     *
     * @return the item lore
     */
    public Component[] getLore() {
        return lore;
    }
}
