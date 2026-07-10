package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.animal.tameable.TameableAnimalMeta;
import org.jetbrains.annotations.Nullable;

public class AbstractNautilusMeta extends TameableAnimalMeta {
    public AbstractNautilusMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isDashing() {
        return metadata.get(MetadataDef.AbstractNautilus.DASH);
    }

    public void setDashing(boolean value) {
        metadata.set(MetadataDef.AbstractNautilus.DASH, value);
    }

}
