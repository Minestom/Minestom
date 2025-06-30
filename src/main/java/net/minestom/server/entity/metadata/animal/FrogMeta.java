package net.minestom.server.entity.metadata.animal;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FrogMeta extends AnimalMeta {
    public FrogMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#FROG_VARIANT} instead.
     */
    @Deprecated
    public @NotNull RegistryKey<FrogVariant> getVariant() {
        return metadata.get(MetadataDef.Frog.VARIANT);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#FROG_VARIANT} instead.
     */
    @Deprecated
    public void setVariant(@NotNull RegistryKey<FrogVariant> value) {
        metadata.set(MetadataDef.Frog.VARIANT, value);
    }

    public @Nullable Integer getTongueTarget() {
        return metadata.get(MetadataDef.Frog.TONGUE_TARGET);
    }

    public void setTongueTarget(@Nullable Integer value) {
        metadata.set(MetadataDef.Frog.TONGUE_TARGET, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(@NotNull DataComponent<T> component) {
        if (component == DataComponents.FROG_VARIANT)
            return (T) getVariant();
        return super.get(component);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> void set(@NotNull DataComponent<T> component, @NotNull T value) {
        if (component == DataComponents.FROG_VARIANT)
            setVariant((RegistryKey<FrogVariant>) value);
        else super.set(component, value);
    }
}
