package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import org.jetbrains.annotations.Nullable;

public class AnimalMeta extends AgeableMobMeta {
    protected AnimalMeta(@Nullable Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

}
