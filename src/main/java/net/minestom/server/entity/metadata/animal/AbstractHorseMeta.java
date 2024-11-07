package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class AbstractHorseMeta extends AnimalMeta {
    protected AbstractHorseMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isTamed() {
        return metadata.get(MetadataDef.AbstractHorse.IS_TAME);
    }

    public void setTamed(boolean value) {
        metadata.set(MetadataDef.AbstractHorse.IS_TAME, value);
    }

    public boolean isSaddled() {
        return metadata.get(MetadataDef.AbstractHorse.IS_SADDLED);
    }

    public void setSaddled(boolean value) {
        metadata.set(MetadataDef.AbstractHorse.IS_SADDLED, value);
    }

    public boolean isHasBred() {
        return metadata.get(MetadataDef.AbstractHorse.HAS_BRED);
    }

    public void setHasBred(boolean value) {
        metadata.set(MetadataDef.AbstractHorse.HAS_BRED, value);
    }

    public boolean isEating() {
        return metadata.get(MetadataDef.AbstractHorse.IS_EATING);
    }

    public void setEating(boolean value) {
        metadata.set(MetadataDef.AbstractHorse.IS_EATING, value);
    }

    public boolean isRearing() {
        return metadata.get(MetadataDef.AbstractHorse.IS_REARING);
    }

    public void setRearing(boolean value) {
        metadata.set(MetadataDef.AbstractHorse.IS_REARING, value);
    }

    public boolean isMouthOpen() {
        return metadata.get(MetadataDef.AbstractHorse.IS_MOUTH_OPEN);
    }

    public void setMouthOpen(boolean value) {
        metadata.set(MetadataDef.AbstractHorse.IS_MOUTH_OPEN, value);
    }

}
