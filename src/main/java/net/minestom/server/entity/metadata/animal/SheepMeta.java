package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class SheepMeta extends AnimalMeta {
    public SheepMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getColor() {
        // TODO replace with Entry
        var entry = (MetadataDef.Entry.Mask) MetadataDef.Sheep.COLOR_ID;
        return getMask(entry.index()) & ((byte) entry.bitMask());
    }

    public void setColor(byte color) {
        // TODO replace with Entry
        var entry = (MetadataDef.Entry.Mask) MetadataDef.Sheep.COLOR_ID;
        byte before = getMask(entry.index());
        byte mask = before;
        mask &= ~((byte) entry.bitMask());
        mask |= (color & ((byte) entry.bitMask()));
        if (mask != before) {
            setMask(entry.index(), mask);
        }
    }

    public boolean isSheared() {
        return metadata.get(MetadataDef.Sheep.IS_SHEARED);
    }

    public void setSheared(boolean value) {
        metadata.set(MetadataDef.Sheep.IS_SHEARED, value);
    }

    // TODO remove
    private byte getMask(int index) {
        return this.metadata.getIndex(index, (byte) 0);
    }

    // TODO remove
    private void setMask(int index, byte mask) {
        this.metadata.setIndex(index, Metadata.Byte(mask));
    }

}
