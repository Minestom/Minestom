package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;

public sealed abstract class AbstractArrowMeta extends EntityMeta permits ArrowMeta, SpectralArrowMeta, ThrownTridentMeta {
    protected AbstractArrowMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isCritical() {
        return get(MetadataDef.AbstractArrow.IS_CRITICAL);
    }

    public void setCritical(boolean value) {
        set(MetadataDef.AbstractArrow.IS_CRITICAL, value);
    }

    public boolean isNoClip() {
        return get(MetadataDef.AbstractArrow.IS_NO_CLIP);
    }

    public void setNoClip(boolean value) {
        set(MetadataDef.AbstractArrow.IS_NO_CLIP, value);
    }

    public byte getPiercingLevel() {
        return get(MetadataDef.AbstractArrow.PIERCING_LEVEL);
    }

    public void setPiercingLevel(byte value) {
        set(MetadataDef.AbstractArrow.PIERCING_LEVEL, value);
    }

    public boolean isInGround() {
        return get(MetadataDef.AbstractArrow.IN_GROUND);
    }

    public void setInGround(boolean value) {
        set(MetadataDef.AbstractArrow.IN_GROUND, value);
    }

}
