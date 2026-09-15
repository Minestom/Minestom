package net.minestom.server.entity.metadata.other;

import net.minestom.server.color.DyeColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.Nullable;

public final class CushionMeta extends EntityMeta {
    public CushionMeta(@Nullable Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public DyeColor getColor() {
        return metadata.get(MetadataDef.Cushion.COLOR);
    }

    public void setColor(DyeColor value) {
        metadata.set(MetadataDef.Cushion.COLOR, value);
    }
}
