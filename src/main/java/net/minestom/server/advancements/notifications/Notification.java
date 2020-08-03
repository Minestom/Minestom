package net.minestom.server.advancements.notifications;

import net.minestom.server.advancements.FrameType;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Represent a message which can be send using the {@link NotificationCenter}
 */
public class Notification {

    private final ColoredText title;
    private final FrameType frameType;
    private final ItemStack icon;

    public Notification(@NotNull ColoredText title, @NotNull FrameType frameType, @NotNull ItemStack icon) {
        this.title = title;
        this.frameType = frameType;
        this.icon = icon;
    }

    public Notification(@NotNull ColoredText title, @NotNull FrameType frameType, @NotNull Material icon) {
        this.title = title;
        this.frameType = frameType;
        this.icon = new ItemStack(icon, (byte) 1);
    }

    /**
     * Get the title of the notification
     *
     * @return the notification title
     */
    @NotNull
    public ColoredText getTitle() {
        return title;
    }

    /**
     * Get the frame type of the notification
     *
     * @return the notification frame type
     */
    @NotNull
    public FrameType getFrameType() {
        return frameType;
    }

    /**
     * Get the icon of the notification
     *
     * @return the notification icon
     */
    @NotNull
    protected ItemStack getIcon() {
        return icon;
    }
}
