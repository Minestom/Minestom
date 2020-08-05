package net.minestom.server.advancements;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class AdvancementRoot extends Advancement {

    public AdvancementRoot(ColoredText title, ColoredText description,
                           ItemStack icon, FrameType frameType,
                           float x, float y,
                           String background) {
        super(title, description, icon, frameType, x, y);
        setBackground(background);
    }

    public AdvancementRoot(ColoredText title, ColoredText description,
                           Material icon, FrameType frameType,
                           float x, float y,
                           String background) {
        super(title, description, icon, frameType, x, y);
        setBackground(background);
    }

}
