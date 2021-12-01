package net.minestom.server.advancements.notifications;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a message which can be sent using the {@link NotificationCenter}.
 */
public record Notification(@NotNull Component title, @NotNull FrameType frameType, @NotNull ItemStack icon) {
    public Notification(@NotNull Component title, @NotNull FrameType frameType, @NotNull Material icon) {
        this(title, frameType, ItemStack.of(icon));
    }

    @Deprecated
    public @NotNull Component getTitle() {
        return title;
    }

    @Deprecated
    public @NotNull FrameType getFrameType() {
        return frameType;
    }
}
