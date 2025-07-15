package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.color.DyeColor;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.registry.RegistryKey;
import org.jspecify.annotations.Nullable;

public class CatMeta extends TameableAnimalMeta {
    private static final DyeColor[] DYE_VALUES = DyeColor.values();

    public CatMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#CAT_VARIANT} instead.
     */
    @Deprecated
    public RegistryKey<CatVariant> getVariant() {
        return metadata.get(MetadataDef.Cat.VARIANT);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#CAT_VARIANT} instead.
     */
    @Deprecated
    public void setVariant(RegistryKey<CatVariant> value) {
        metadata.set(MetadataDef.Cat.VARIANT, value);
    }

    public boolean isLying() {
        return metadata.get(MetadataDef.Cat.IS_LYING);
    }

    public void setLying(boolean value) {
        metadata.set(MetadataDef.Cat.IS_LYING, value);
    }

    public boolean isRelaxed() {
        return metadata.get(MetadataDef.Cat.IS_RELAXED);
    }

    public void setRelaxed(boolean value) {
        metadata.set(MetadataDef.Cat.IS_RELAXED, value);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#CAT_COLLAR} instead.
     */
    @Deprecated
    public DyeColor getCollarColor() {
        return DYE_VALUES[metadata.get(MetadataDef.Cat.COLLAR_COLOR)];
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#CAT_COLLAR} instead.
     */
    @Deprecated
    public void setCollarColor(DyeColor value) {
        metadata.set(MetadataDef.Cat.COLLAR_COLOR, value.ordinal());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(DataComponent<T> component) {
        if (component == DataComponents.CAT_VARIANT)
            return (T) getVariant();
        if (component == DataComponents.CAT_COLLAR)
            return (T) getCollarColor();
        return super.get(component);
    }

    @Override
    protected <T> void set(DataComponent<T> component, T value) {
        if (component == DataComponents.CAT_VARIANT)
            setVariant((RegistryKey<CatVariant>) value);
        else if (component == DataComponents.CAT_COLLAR)
            setCollarColor((DyeColor) value);
        else super.set(component, value);
    }

}
