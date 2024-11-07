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
        // TODO: remove MetadataDef.Sheep.MASK and replace with MetadataDef.Sheep.COLOR_ID
        byte bitMask = (byte) ((MetadataDef.Entry.Mask) MetadataDef.Sheep.COLOR_ID).bitMask();
        return metadata.get(MetadataDef.Sheep.MASK) & bitMask;
    }

    public void setColor(byte color) {
        // TODO: remove MetadataDef.Sheep.MASK and replace with MetadataDef.Sheep.COLOR_ID
        byte bitMask = (byte) ((MetadataDef.Entry.Mask) MetadataDef.Sheep.COLOR_ID).bitMask();
        byte before = metadata.get(MetadataDef.Sheep.MASK);
        byte mask = before;
        mask &= ~(bitMask);
        mask |= (color & bitMask);
        if (mask != before) {
            metadata.set(MetadataDef.Sheep.MASK, mask);
        }
    }

    public boolean isSheared() {
        return metadata.get(MetadataDef.Sheep.IS_SHEARED);
    }

    public void setSheared(boolean value) {
        metadata.set(MetadataDef.Sheep.IS_SHEARED, value);
    }

}
