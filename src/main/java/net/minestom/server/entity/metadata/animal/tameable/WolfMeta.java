package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.color.DyeColor;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.registry.RegistryKey;
import org.jspecify.annotations.Nullable;

public class WolfMeta extends TameableAnimalMeta {
    public WolfMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isBegging() {
        return metadata.get(MetadataDef.Wolf.IS_BEGGING);
    }

    public void setBegging(boolean value) {
        metadata.set(MetadataDef.Wolf.IS_BEGGING, value);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#WOLF_COLLAR} instead.
     */
    @Deprecated
    public DyeColor getCollarColor() {
        return DyeColor.values()[metadata.get(MetadataDef.Wolf.COLLAR_COLOR)];
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#WOLF_COLLAR} instead.
     */
    @Deprecated
    public void setCollarColor(DyeColor value) {
        metadata.set(MetadataDef.Wolf.COLLAR_COLOR, value.ordinal());
    }

    public int getAngerTime() {
        return metadata.get(MetadataDef.Wolf.ANGER_TIME);
    }

    public void setAngerTime(int value) {
        metadata.set(MetadataDef.Wolf.ANGER_TIME, value);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#WOLF_VARIANT} instead.
     */
    @Deprecated
    public RegistryKey<WolfVariant> getVariant() {
        return metadata.get(MetadataDef.Wolf.VARIANT);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#WOLF_VARIANT} instead.
     */
    @Deprecated
    public void setVariant(RegistryKey<WolfVariant> value) {
        metadata.set(MetadataDef.Wolf.VARIANT, value);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#WOLF_SOUND_VARIANT} instead.
     */
    @Deprecated
    public RegistryKey<WolfSoundVariant> getSoundVariant() {
        return metadata.get(MetadataDef.Wolf.SOUND_VARIANT);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#WOLF_SOUND_VARIANT} instead.
     */
    @Deprecated
    public void setSoundVariant(RegistryKey<WolfSoundVariant> value) {
        metadata.set(MetadataDef.Wolf.SOUND_VARIANT, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(DataComponent<T> component) {
        if (component == DataComponents.WOLF_VARIANT)
            return (T) getVariant();
        if (component == DataComponents.WOLF_SOUND_VARIANT)
            return (T) getSoundVariant();
        if (component == DataComponents.WOLF_COLLAR)
            return (T) getCollarColor();
        return super.get(component);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> void set(DataComponent<T> component, T value) {
        if (component == DataComponents.WOLF_VARIANT)
            setVariant((RegistryKey<WolfVariant>) value);
        else if (component == DataComponents.WOLF_SOUND_VARIANT)
            setSoundVariant((RegistryKey<WolfSoundVariant>) value);
        else if (component == DataComponents.WOLF_COLLAR)
            setCollarColor((DyeColor) value);
        else super.set(component, value);
    }
}
