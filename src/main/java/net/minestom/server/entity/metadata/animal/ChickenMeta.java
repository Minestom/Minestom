package net.minestom.server.entity.metadata.animal;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.registry.RegistryKey;
import org.jspecify.annotations.Nullable;

public class ChickenMeta extends AnimalMeta {
    public ChickenMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }


    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#CHICKEN_VARIANT} instead.
     */
    @Deprecated
    public RegistryKey<ChickenVariant> getVariant() {
        return metadata.get(MetadataDef.Chicken.VARIANT);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#CHICKEN_VARIANT} instead.
     */
    @Deprecated
    public void setVariant(RegistryKey<ChickenVariant> variant) {
        metadata.set(MetadataDef.Chicken.VARIANT, variant);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(DataComponent<T> component) {
        if (component == DataComponents.CHICKEN_VARIANT)
            return (T) getVariant();
        return super.get(component);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> void set(DataComponent<T> component, T value) {
        if (component == DataComponents.CHICKEN_VARIANT)
            setVariant((RegistryKey<ChickenVariant>) value);
        else super.set(component, value);
    }

}
