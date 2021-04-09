package net.minestom.server.advancements;

import net.kyori.adventure.text.Component;
import net.minestom.server.chat.JsonMessage;
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

    /**
     * @deprecated Use {@link #AdvancementRoot(Component, Component, ItemStack, FrameType, float, float, String)}
     */
    @Deprecated
    public AdvancementRoot(@NotNull JsonMessage title, @NotNull JsonMessage description,
                           @NotNull ItemStack icon, @NotNull FrameType frameType,
                           float x, float y,
                           @Nullable String background) {
        super(title, description, icon, frameType, x, y);
        setBackground(background);
    }

    /**
     * @deprecated Use {@link #AdvancementRoot(Component, Component, Material, FrameType, float, float, String)}
     */
    @Deprecated
    public AdvancementRoot(@NotNull JsonMessage title, @NotNull JsonMessage description,
                           @NotNull Material icon, FrameType frameType,
                           float x, float y,
                           @Nullable String background) {
        super(title, description, icon, frameType, x, y);
        setBackground(background);
    }

    public AdvancementRoot(@NotNull Component title, @NotNull Component description,
                           @NotNull ItemStack icon, @NotNull FrameType frameType,
                           float x, float y,
                           @Nullable String background) {
        super(title, description, icon, frameType, x, y);
        setBackground(background);
    }

    public AdvancementRoot(@NotNull Component title, @NotNull Component description,
                           @NotNull Material icon, FrameType frameType,
                           float x, float y,
                           @Nullable String background) {
        super(title, description, icon, frameType, x, y);
        setBackground(background);
    }

}
