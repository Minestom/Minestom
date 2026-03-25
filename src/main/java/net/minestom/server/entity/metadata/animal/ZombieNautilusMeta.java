package net.minestom.server.entity.metadata.animal;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

public class ZombieNautilusMeta extends AbstractNautilusMeta {
    public ZombieNautilusMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#ZOMBIE_NAUTILUS_VARIANT} instead.
     */
    @Deprecated
    public RegistryKey<ZombieNautilusVariant> getVariant() {
        return this.metadata.get(MetadataDef.ZombieNautilus.VARIANT);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#ZOMBIE_NAUTILUS_VARIANT} instead.
     */
    @Deprecated
    public void setVariant(RegistryKey<ZombieNautilusVariant> value) {
        this.metadata.set(MetadataDef.ZombieNautilus.VARIANT, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(DataComponent<T> component) {
        if (component == DataComponents.ZOMBIE_NAUTILUS_VARIANT) {
            return (T) getVariant();
        }
        return super.get(component);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> void set(DataComponent<T> component, T value) {
        if (component == DataComponents.ZOMBIE_NAUTILUS_VARIANT) {
            setVariant((RegistryKey<ZombieNautilusVariant>) value);
        } else super.set(component, value);
    }
}
