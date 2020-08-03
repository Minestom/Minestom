package net.minestom.server.advancements.notifications;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.AdvancementsPacket;
import org.jetbrains.annotations.NotNull;

/**
 * @author Lukas Mansour (Articdive)
 */
public class AdvancementNotification {
    private final ColoredText title;
    private final ItemStack icon;
    private final AdvancementsPacket.FrameType frameType;

    public AdvancementNotification(@NotNull ColoredText title, @NotNull ItemStack icon, @NotNull AdvancementsPacket.FrameType frameType) {
        this.title = title;
        this.icon = icon;
        this.frameType = frameType;
    }

    @NotNull
    public ColoredText getTitle() {
        return title;
    }

    @NotNull
    public ItemStack getIcon() {
        return icon;
    }

    @NotNull
    public AdvancementsPacket.FrameType getFrameType() {
        return frameType;
    }
}
