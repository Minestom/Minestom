package net.minestom.server.entity.metadata.display;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemDisplayMeta extends AbstractDisplayMeta {
    public static final byte OFFSET = AbstractDisplayMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 2;

    public ItemDisplayMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @NotNull ItemStack getItemStack() {
        return super.metadata.getIndex(OFFSET, ItemStack.AIR);
    }

    public void setItemStack(@NotNull ItemStack value) {
        super.metadata.setIndex(OFFSET, Metadata.ItemStack(value));
    }

    public @NotNull DisplayContext getDisplayContext() {
        return DisplayContext.VALUES[super.metadata.getIndex(OFFSET + 1, (byte) 0)];
    }

    public void setDisplayContext(@NotNull DisplayContext value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Byte((byte) value.ordinal()));
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

        private static final DisplayContext[] VALUES = values(); //Microtus - update java keyword usage

    }
}
