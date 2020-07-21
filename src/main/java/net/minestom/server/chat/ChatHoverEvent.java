package net.minestom.server.chat;

import com.google.gson.JsonObject;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;

/**
 * Represent a hover event for a specific portion of the message
 */
public class ChatHoverEvent {

    private String action;
    private String value;
    private JsonObject valueObject;
    private boolean isJson;

    private ChatHoverEvent(String action, String value) {
        this.action = action;
        this.value = value;
    }

    private ChatHoverEvent(String action, JsonObject valueObject) {
        this.action = action;
        this.valueObject = valueObject;
        this.isJson = true;
    }

    /**
     * Show a {@link ColoredText} when hovered
     *
     * @param text the text to show
     * @return the chat hover event
     */
    public static ChatHoverEvent showText(ColoredText text) {
        return new ChatHoverEvent("show_text", text.getJsonObject());
    }

    /**
     * Show a raw text when hovered
     *
     * @param text the text to show
     * @return the chat hover event
     */
    public static ChatHoverEvent showText(String text) {
        return new ChatHoverEvent("show_text", text);
    }

    /**
     * Show an item when hovered
     *
     * @param itemStack the item to show
     * @return the chat hover event
     */
    public static ChatHoverEvent showItem(ItemStack itemStack) {
        return new ChatHoverEvent("show_item", "{id:35,Damage:5,Count:2,tag:{display:{Name:Testing}}}");
    }

    /**
     * Show an entity when hovered
     *
     * @param entity the entity to show
     * @return the chat hover event
     */
    public static ChatHoverEvent showEntity(Entity entity) {
        final String id = entity.getUuid().toString();
        final String type = EntityType.fromId(entity.getEntityType())
                .getNamespaceID().replace("minecraft:", "");
        // TODO name

        JsonObject object = new JsonObject();
        object.addProperty("id", id);
        object.addProperty("type", type);
        return new ChatHoverEvent("show_entity", object);
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
}
