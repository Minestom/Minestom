package net.minestom.server.entity.metadata.animal;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PigMeta extends AnimalMeta {
    public PigMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getTimeToBoost() {
        return metadata.get(MetadataDef.Pig.BOOST_TIME);
    }

    public void setTimeToBoost(int value) {
        metadata.set(MetadataDef.Pig.BOOST_TIME, value);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#PIG_VARIANT} instead.
     */
    @Deprecated
    public @NotNull RegistryKey<PigVariant> getVariant() {
        return metadata.get(MetadataDef.Pig.VARIANT);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#PIG_VARIANT} instead.
     */
    @Deprecated
    public void setVariant(@NotNull RegistryKey<PigVariant> value) {
        metadata.set(MetadataDef.Pig.VARIANT, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(@NotNull DataComponent<T> component) {
        if (component == DataComponents.PIG_VARIANT)
            return (T) getVariant();
        return super.get(component);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> void set(@NotNull DataComponent<T> component, @NotNull T value) {
        if (component == DataComponents.PIG_VARIANT)
            setVariant((RegistryKey<PigVariant>) value);
        else super.set(component, value);
    }

}
