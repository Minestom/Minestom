package net.minestom.server.chat;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.UUID;

/**
 * Represents a hover event for a specific portion of the message.
 * @deprecated Use {@link HoverEvent}
 */
@Deprecated
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
    public String getValue() {
        return value;
    }

    @Nullable
    public JsonObject getValueObject() {
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
        HoverEvent<HoverEvent.ShowItem> event = HoverEvent.showItem(itemStack.getMaterial().key(), itemStack.getAmount());
        JsonObject obj = GsonComponentSerializer.gson().serializer().toJsonTree(Component.empty().hoverEvent(event)).getAsJsonObject();
        obj = obj.get("hoverEvent").getAsJsonObject().get("contents").getAsJsonObject();

        if (itemStack.getItemMeta() != null) {
            NBTCompound compound = new NBTCompound();
            itemStack.getItemMeta().write(compound);
            obj.add("tag", new JsonPrimitive(compound.toSNBT()));
        }

        return new ChatHoverEvent("show_item", obj);
    }

    /**
     * Shows an entity when hovered.
     *
     * @param entity the entity to show
     * @return the chat hover event
     */
    @NotNull
    public static ChatHoverEvent showEntity(@NotNull Entity entity) {
        HoverEvent<HoverEvent.ShowEntity> event = HoverEvent.showEntity(entity.getEntityType().key(), entity.getUuid());
        JsonObject obj = GsonComponentSerializer.gson().serializer().toJsonTree(Component.empty().hoverEvent(event)).getAsJsonObject();
        return new ChatHoverEvent("show_entity", obj.get("hoverEvent").getAsJsonObject().get("contents").getAsJsonObject());
    }

    public static ChatHoverEvent showEntity(UUID uuid, EntityType entityType) {
        HoverEvent<HoverEvent.ShowEntity> event = HoverEvent.showEntity(entityType.key(), uuid);
        JsonObject obj = GsonComponentSerializer.gson().serializer().toJsonTree(Component.empty().hoverEvent(event)).getAsJsonObject();
        return new ChatHoverEvent("show_entity", obj.get("hoverEvent").getAsJsonObject().get("contents").getAsJsonObject());
    }
}
