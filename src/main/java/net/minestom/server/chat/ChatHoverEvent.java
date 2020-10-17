package net.minestom.server.chat;

import com.google.gson.JsonObject;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

/**
 * Represents a hover event for a specific portion of the message.
 */
public class ChatHoverEvent {

    private final String action;
    private String value;
    private JsonObject valueObject;
    private final boolean isJson;

    private ChatHoverEvent(String action, String value) {
        this.action = action;
        this.value = value;
        this.isJson = false;
    }

    private ChatHoverEvent(String action, JsonObject valueObject) {
        this.action = action;
        this.valueObject = valueObject;
        this.isJson = true;
    }

    protected String getAction() {
        return action;
    }

    protected String getValue() {
        return value;
    }

    protected JsonObject getValueObject() {
        return valueObject;
    }

    protected boolean isJson() {
        return isJson;
    }

    /**
     * Shows a {@link ColoredText} when hovered.
     *
     * @param text the text to show
     * @return the chat hover event
     */
    public static ChatHoverEvent showText(ColoredText text) {
        return new ChatHoverEvent("show_text", text.getJsonObject());
    }

    /**
     * Shows a raw text when hovered.
     *
     * @param text the text to show
     * @return the chat hover event
     */
    public static ChatHoverEvent showText(String text) {
        return new ChatHoverEvent("show_text", text);
    }

    /**
     * Shows an item when hovered.
     *
     * @param itemStack the item to show
     * @return the chat hover event
     */
    public static ChatHoverEvent showItem(ItemStack itemStack) {
        final String json = itemStack.toNBT().toSNBT();
        return new ChatHoverEvent("show_item", json);
    }

    /**
     * Shows an entity when hovered.
     *
     * @param entity the entity to show
     * @return the chat hover event
     */
    public static ChatHoverEvent showEntity(Entity entity) {
        NBTCompound compound = new NBTCompound()
                .setString("id", entity.getUuid().toString())
                .setString("type", entity.getEntityType().getNamespaceID());
        return new ChatHoverEvent("show_entity", compound.toSNBT());
    }
}
