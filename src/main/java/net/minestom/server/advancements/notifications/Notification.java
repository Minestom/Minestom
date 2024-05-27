package net.minestom.server.advancements.notifications;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a message which can be sent using the {@link NotificationCenter}.
 * @since 1.0.0
 * @deprecated As of Minestom 22a8ccabfae38c53df0605000aa7eed49765c1ab, because the Maintainability is very hard and
 *      can break everytime from Mojang side because bad api design use {@link net.minestom.server.notifications.Notification#builder()} instead.
 */
@Deprecated(since = "1.4.1", forRemoval = true)
public record Notification(@NotNull Component title, @NotNull FrameType frameType, @NotNull ItemStack icon) {
    public Notification(@NotNull Component title, @NotNull FrameType frameType, @NotNull Material icon) {
        this(title, frameType, ItemStack.of(icon));
    }
}
