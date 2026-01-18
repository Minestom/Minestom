package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.animal.tameable.TameableAnimalMeta;

public sealed abstract class AbstractNautilusMeta extends TameableAnimalMeta permits NautilusMeta, ZombieNautilusMeta {
    protected AbstractNautilusMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isDashing() {
        return metadata.get(MetadataDef.AbstractNautilus.DASH);
    }

    public void setDashing(boolean value) {
        metadata.set(MetadataDef.AbstractNautilus.DASH, value);
    }

}
