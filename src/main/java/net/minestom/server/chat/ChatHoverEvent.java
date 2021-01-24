package net.minestom.server.chat;

import com.google.gson.JsonObject;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

/**
 * Represents a hover event for a specific portion of the message.
 */
public class ChatHoverEvent {

    private final String action;
    private final String value;
    private final JsonObject valueObject;
    private final boolean isJson;

    private ChatHoverEvent(@NotNull String action, @NotNull String value) {
        this.action = action;
        this.value = value;
        this.valueObject = null;
        this.isJson = false;
    }

    private ChatHoverEvent(@NotNull String action, @NotNull JsonObject valueObject) {
        this.action = action;
        this.value = null;
        this.valueObject = valueObject;
        this.isJson = true;
    }

    @NotNull
    protected String getAction() {
        return action;
    }

    @Nullable
    protected String getValue() {
        return value;
    }

    @Nullable
    protected JsonObject getValueObject() {
        return valueObject;
    }

    protected boolean isJson() {
        return isJson;
    }

    /**
     * Shows a {@link JsonMessage} when hovered.
     *
     * @param text the text to show
     * @return the chat hover event
     */
    @NotNull
    public static ChatHoverEvent showText(@NotNull JsonMessage text) {
        return new ChatHoverEvent("show_text", text.getJsonObject());
    }

    /**
     * Shows a raw text when hovered.
     *
     * @param text the text to show
     * @return the chat hover event
     */
    @NotNull
    public static ChatHoverEvent showText(@NotNull String text) {
        return new ChatHoverEvent("show_text", text);
    }

    /**
     * Shows an item when hovered.
     *
     * @param itemStack the item to show
     * @return the chat hover event
     */
    @NotNull
    public static ChatHoverEvent showItem(@NotNull ItemStack itemStack) {
        final String json = itemStack.toNBT().toSNBT();
        return new ChatHoverEvent("show_item", json);
    }

    /**
     * Shows an entity when hovered.
     *
     * @param entity the entity to show
     * @return the chat hover event
     */
    @NotNull
    public static ChatHoverEvent showEntity(@NotNull Entity entity) {
        NBTCompound compound = new NBTCompound()
                .setString("id", entity.getUuid().toString())
                .setString("type", entity.getEntityType().getNamespaceID());
        return new ChatHoverEvent("show_entity", compound.toSNBT());
    }
}
