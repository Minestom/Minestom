package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.registry.RegistryKey;

public final class ZombieNautilusMeta extends AbstractNautilusMeta {
    public ZombieNautilusMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public RegistryKey<ZombieNautilusVariant> getVariant() {
        return this.get(MetadataDef.ZombieNautilus.VARIANT);
    }

    public void setVariant(RegistryKey<ZombieNautilusVariant> value) {
        this.set(MetadataDef.ZombieNautilus.VARIANT, value);
    }

}
