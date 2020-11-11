package net.minestom.server.advancements;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an {@link Advancement} which is the root of an {@link AdvancementTab}.
 * Every tab requires one since advancements needs to be linked to a parent.
 * <p>
 * The difference between this and an {@link Advancement} is that the root is responsible for the tab background.
 */
public class AdvancementRoot extends Advancement {

    public AdvancementRoot(@NotNull ColoredText title, @NotNull ColoredText description,
                           @NotNull ItemStack icon, @NotNull FrameType frameType,
                           float x, float y,
                           @Nullable String background) {
        super(title, description, icon, frameType, x, y);
        setBackground(background);
    }

    public AdvancementRoot(@NotNull ColoredText title, @NotNull ColoredText description,
                           @NotNull Material icon, FrameType frameType,
                           float x, float y,
                           @Nullable String background) {
        super(title, description, icon, frameType, x, y);
        setBackground(background);
    }

}
