package net.minestom.server.entity.metadata.animal;

import net.minestom.server.color.DyeColor;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jspecify.annotations.Nullable;

public class SheepMeta extends AnimalMeta {
    private static final DyeColor[] DYE_VALUES = DyeColor.values();

    public SheepMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#SHEEP_COLOR} instead.
     */
    @Deprecated
    public DyeColor getColor() {
        return DYE_VALUES[metadata.get(MetadataDef.Sheep.COLOR_ID)];
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#SHEEP_COLOR} instead.
     */
    @Deprecated
    public void setColor(DyeColor color) {
        metadata.set(MetadataDef.Sheep.COLOR_ID, (byte) color.ordinal());
    }

    public boolean isSheared() {
        return metadata.get(MetadataDef.Sheep.IS_SHEARED);
    }

    public void setSheared(boolean value) {
        metadata.set(MetadataDef.Sheep.IS_SHEARED, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(DataComponent<T> component) {
        if (component == DataComponents.SHEEP_COLOR)
            return (T) getColor();
        return super.get(component);
    }

    @Override
    protected <T> void set(DataComponent<T> component, T value) {
        if (component == DataComponents.SHEEP_COLOR)
            setColor((DyeColor) value);
        else super.set(component, value);
    }

}
