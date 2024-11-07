package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class SheepMeta extends AnimalMeta {
    public SheepMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getColor() {
        // TODO: make COLOR_ID is 4 bits
        byte bitMask = (byte) ((MetadataDef.Entry.Mask) MetadataDef.Sheep.COLOR_ID).bitMask();
        return metadata.get(MetadataDef.Sheep.SHEEP_FLAGS) & bitMask;
    }

    public void setColor(byte color) {
        // TODO: make COLOR_ID is 4 bits
        byte bitMask = (byte) ((MetadataDef.Entry.Mask) MetadataDef.Sheep.COLOR_ID).bitMask();
        byte before = metadata.get(MetadataDef.Sheep.SHEEP_FLAGS);
        byte mask = before;
        mask &= ~(bitMask);
        mask |= (color & bitMask);
        if (mask != before) {
            metadata.set(MetadataDef.Sheep.SHEEP_FLAGS, mask);
        }
    }

    public boolean isSheared() {
        return metadata.get(MetadataDef.Sheep.IS_SHEARED);
    }

    public void setSheared(boolean value) {
        metadata.set(MetadataDef.Sheep.IS_SHEARED, value);
    }

}
