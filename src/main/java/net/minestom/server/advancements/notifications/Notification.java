package net.minestom.server.advancements.notifications;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a message which can be send using the {@link NotificationCenter}.
 */
public class Notification {

    private final Component title;
    private final FrameType frameType;
    private final ItemStack icon;

    /**
     * @deprecated Use {@link #Notification(Component, FrameType, ItemStack)}
     */
    @Deprecated
    public Notification(@NotNull JsonMessage title, @NotNull FrameType frameType, @NotNull ItemStack icon) {
        this(title.asComponent(), frameType, icon);
    }

    /**
     * @deprecated Use {@link #Notification(Component, FrameType, Material)}
     */
    @Deprecated
    public Notification(@NotNull JsonMessage title, @NotNull FrameType frameType, @NotNull Material icon) {
        this(title.asComponent(), frameType, icon);
    }

    public Notification(@NotNull Component title, @NotNull FrameType frameType, @NotNull Material icon) {
        this(title, frameType, new ItemStack(icon, (byte) 1));
    }

    public Notification(@NotNull Component title, @NotNull FrameType frameType, @NotNull ItemStack icon) {
        this.title = title;
        this.frameType = frameType;
        this.icon = icon;
    }

    /**
     * Gets the title of the notification.
     *
     * @return the notification title
     *
     * @deprecated Use {@link #getTitle()}
     */
    @NotNull
    @Deprecated
    public JsonMessage getTitleJson() {
        return JsonMessage.fromComponent(title);
    }

    /**
     * Gets the title of the notification.
     *
     * @return the notification title
     */
    public Component getTitle() {
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
