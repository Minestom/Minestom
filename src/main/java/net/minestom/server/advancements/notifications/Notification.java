package net.minestom.server.advancements.notifications;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
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

    public Notification(@NotNull Component title, @NotNull FrameType frameType, @NotNull Material icon) {
        this(title, frameType, ItemStack.of(icon));
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
