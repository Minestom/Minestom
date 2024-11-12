package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;

public class PrimedTntMeta extends EntityMeta {
    public PrimedTntMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getFuseTime() {
        return metadata.get(MetadataDef.PrimedTnt.FUSE_TIME);
    }

    public void setFuseTime(int value) {
        metadata.set(MetadataDef.PrimedTnt.FUSE_TIME, value);
    }

}
