package net.minestom.server.entity.metadata.display;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemDisplayMeta extends AbstractDisplayMeta {
    public ItemDisplayMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @NotNull ItemStack getItemStack() {
        return metadata.get(MetadataDef.ItemDisplay.DISPLAYED_ITEM);
    }

    public void setItemStack(@NotNull ItemStack value) {
        metadata.set(MetadataDef.ItemDisplay.DISPLAYED_ITEM, value);
    }

    public @NotNull DisplayContext getDisplayContext() {
        return DisplayContext.VALUES[metadata.get(MetadataDef.ItemDisplay.DISPLAY_TYPE)];
    }

    public void setDisplayContext(@NotNull DisplayContext value) {
        metadata.set(MetadataDef.ItemDisplay.DISPLAY_TYPE, (byte) value.ordinal());
    }

    public enum DisplayContext {
        NONE,
        THIRD_PERSON_LEFT_HAND,
        THIRD_PERSON_RIGHT_HAND,
        FIRST_PERSON_LEFT_HAND,
        FIRST_PERSON_RIGHT_HAND,
        HEAD,
        GUI,
        GROUND,
        FIXED;

        private final static DisplayContext[] VALUES = values();

    }
}
