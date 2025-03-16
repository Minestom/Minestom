package net.minestom.server.entity.metadata.animal;

import net.minestom.server.color.DyeColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class SheepMeta extends AnimalMeta {
    private static final DyeColor[] DYE_VALUES = DyeColor.values();

    public SheepMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#SHEEP_COLOR} instead.
     */
    public @NotNull DyeColor getColor() {
        return DYE_VALUES[metadata.get(MetadataDef.Sheep.COLOR_ID)];
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#SHEEP_COLOR} instead.
     */
    public void setColor(@NotNull DyeColor color) {
        metadata.set(MetadataDef.Sheep.COLOR_ID, (byte) color.ordinal());
    }

    public boolean isSheared() {
        return metadata.get(MetadataDef.Sheep.IS_SHEARED);
    }

    public void setSheared(boolean value) {
        metadata.set(MetadataDef.Sheep.IS_SHEARED, value);
    }

}
