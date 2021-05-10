package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import org.jetbrains.annotations.NotNull;

public class AnimalMeta extends AgeableMobMeta {

    protected AnimalMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

}
