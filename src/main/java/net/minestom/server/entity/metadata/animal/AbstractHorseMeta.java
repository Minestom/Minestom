package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public sealed abstract class AbstractHorseMeta extends AnimalMeta permits CamelMeta, ChestedHorseMeta, HorseMeta, SkeletonHorseMeta, ZombieHorseMeta {
    protected AbstractHorseMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isTamed() {
        return get(MetadataDef.AbstractHorse.IS_TAME);
    }

    public void setTamed(boolean value) {
        set(MetadataDef.AbstractHorse.IS_TAME, value);
    }

    public boolean isHasBred() {
        return get(MetadataDef.AbstractHorse.HAS_BRED);
    }

    public void setHasBred(boolean value) {
        set(MetadataDef.AbstractHorse.HAS_BRED, value);
    }

    public boolean isEating() {
        return get(MetadataDef.AbstractHorse.IS_EATING);
    }

    public void setEating(boolean value) {
        set(MetadataDef.AbstractHorse.IS_EATING, value);
    }

    public boolean isRearing() {
        return get(MetadataDef.AbstractHorse.IS_REARING);
    }

    public void setRearing(boolean value) {
        set(MetadataDef.AbstractHorse.IS_REARING, value);
    }

    public boolean isMouthOpen() {
        return get(MetadataDef.AbstractHorse.IS_MOUTH_OPEN);
    }

    public void setMouthOpen(boolean value) {
        set(MetadataDef.AbstractHorse.IS_MOUTH_OPEN, value);
    }

}
