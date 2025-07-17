package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;

public class ExperienceOrbMeta extends EntityMeta {

    public ExperienceOrbMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getValue() {
        return metadata.get(MetadataDef.ExperienceOrb.VALUE);
    }

    public void setValue(int value) {
        metadata.set(MetadataDef.ExperienceOrb.VALUE, value);
    }
}
