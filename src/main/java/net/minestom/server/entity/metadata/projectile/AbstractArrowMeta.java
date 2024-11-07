package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;

public class AbstractArrowMeta extends EntityMeta {
    protected AbstractArrowMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isCritical() {
        return metadata.get(MetadataDef.AbstractArrow.IS_CRITICAL);
    }

    public void setCritical(boolean value) {
        metadata.set(MetadataDef.AbstractArrow.IS_CRITICAL, value);
    }

    public boolean isNoClip() {
        return metadata.get(MetadataDef.AbstractArrow.IS_NO_CLIP);
    }

    public void setNoClip(boolean value) {
        metadata.set(MetadataDef.AbstractArrow.IS_NO_CLIP, value);
    }

    public byte getPiercingLevel() {
        return metadata.get(MetadataDef.AbstractArrow.PIERCING_LEVEL);
    }

    public void setPiercingLevel(byte value) {
        metadata.set(MetadataDef.AbstractArrow.PIERCING_LEVEL, value);
    }

}
