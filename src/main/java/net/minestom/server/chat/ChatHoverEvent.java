package net.minestom.server.chat;

import com.google.gson.JsonObject;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;

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

    public static ChatHoverEvent showText(ColoredText text) {
        return new ChatHoverEvent("show_text", text.getJsonObject());
    }


    public static ChatHoverEvent showText(String text) {
        return new ChatHoverEvent("show_text", text);
    }

    public static ChatHoverEvent showItem(ItemStack itemStack) {
        throw new UnsupportedOperationException("Feature in progress");
        //return new ChatHoverEvent("show_item", parsedItem);
    }

    public static ChatHoverEvent showEntity(Entity entity) {
        throw new UnsupportedOperationException("Feature in progress");
        //return new ChatHoverEvent("show_entity", parsedEntity);
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
