package net.minestom.server.entity.metadata.animal;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.registry.DynamicRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CowMeta extends AnimalMeta {
    public CowMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#COW_VARIANT} instead.
     */
    @Deprecated
    public @NotNull DynamicRegistry.Key<CowVariant> getVariant() {
        return metadata.get(MetadataDef.Cow.VARIANT);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#COW_VARIANT} instead.
     */
    @Deprecated
    public void setVariant(@NotNull DynamicRegistry.Key<CowVariant> variant) {
        metadata.set(MetadataDef.Cow.VARIANT, variant);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(@NotNull DataComponent<T> component) {
        if (component == DataComponents.COW_VARIANT)
            return (T) getVariant();
        return super.get(component);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> void set(@NotNull DataComponent<T> component, @NotNull T value) {
        if (component == DataComponents.COW_VARIANT)
            setVariant((DynamicRegistry.Key<CowVariant>) value);
        else super.set(component, value);
    }

}
