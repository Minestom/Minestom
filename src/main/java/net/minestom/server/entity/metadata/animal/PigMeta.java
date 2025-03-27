package net.minestom.server.entity.metadata.animal;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.registry.DynamicRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PigMeta extends AnimalMeta {
    public PigMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isHasSaddle() {
        return metadata.get(MetadataDef.Pig.HAS_SADDLE);
    }

    public void setHasSaddle(boolean value) {
        metadata.set(MetadataDef.Pig.HAS_SADDLE, value);
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
    public @NotNull DynamicRegistry.Key<PigVariant> getVariant() {
        return metadata.get(MetadataDef.Pig.VARIANT);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#PIG_VARIANT} instead.
     */
    @Deprecated
    public void setVariant(@NotNull DynamicRegistry.Key<PigVariant> value) {
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
            setVariant((DynamicRegistry.Key<PigVariant>) value);
        else super.set(component, value);
    }

}
