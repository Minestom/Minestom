package net.minestom.server.entity.metadata.display;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.item.ItemStack;

public class ItemDisplayMeta extends AbstractDisplayMeta {
    public ItemDisplayMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public ItemStack getItemStack() {
        return metadata.get(MetadataDef.ItemDisplay.DISPLAYED_ITEM);
    }

    public void setItemStack(ItemStack value) {
        metadata.set(MetadataDef.ItemDisplay.DISPLAYED_ITEM, value);
    }

    public DisplayContext getDisplayContext() {
        return DisplayContext.VALUES[metadata.get(MetadataDef.ItemDisplay.DISPLAY_TYPE)];
    }

    public void setDisplayContext(DisplayContext value) {
        metadata.set(MetadataDef.ItemDisplay.DISPLAY_TYPE, (byte) value.ordinal());
    }

    public enum DisplayContext {
        NONE,
        THIRDPERSON_LEFTHAND,
        THIRDPERSON_RIGHTHAND,
        FIRSTPERSON_LEFTHAND,
        FIRSTPERSON_RIGHTHAND,
        HEAD,
        GUI,
        GROUND,
        FIXED,
        ON_SHELF;

        private final static DisplayContext[] VALUES = values();

    }
}
