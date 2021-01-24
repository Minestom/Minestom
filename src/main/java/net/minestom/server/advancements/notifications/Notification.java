package net.minestom.server.advancements.notifications;

import net.minestom.server.advancements.FrameType;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a message which can be send using the {@link NotificationCenter}.
 */
public class Notification {

    private final JsonMessage title;
    private final FrameType frameType;
    private final ItemStack icon;

    public Notification(@NotNull JsonMessage title, @NotNull FrameType frameType, @NotNull ItemStack icon) {
        this.title = title;
        this.frameType = frameType;
        this.icon = icon;
    }

    public Notification(@NotNull JsonMessage title, @NotNull FrameType frameType, @NotNull Material icon) {
        this.title = title;
        this.frameType = frameType;
        this.icon = new ItemStack(icon, (byte) 1);
    }

    /**
     * Gets the title of the notification.
     *
     * @return the notification title
     */
    @NotNull
    public JsonMessage getTitle() {
        return title;
    }

    /**
     * Gets the {@link FrameType} of the notification.
     *
     * @return the notification frame type
     */
    @NotNull
    public FrameType getFrameType() {
        return frameType;
    }

    /**
     * Gets the icon of the notification.
     *
     * @return the notification icon
     */
    @NotNull
    protected ItemStack getIcon() {
        return icon;
    }
}
