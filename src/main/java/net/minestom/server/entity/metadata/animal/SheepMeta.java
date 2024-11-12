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
        return metadata.get(MetadataDef.Sheep.COLOR_ID);
    }

    public void setColor(byte color) {
        metadata.set(MetadataDef.Sheep.COLOR_ID, color);
    }

    public boolean isSheared() {
        return metadata.get(MetadataDef.Sheep.IS_SHEARED);
    }

    public void setSheared(boolean value) {
        metadata.set(MetadataDef.Sheep.IS_SHEARED, value);
    }

}
