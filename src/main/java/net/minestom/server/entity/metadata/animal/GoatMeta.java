package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class GoatMeta extends AnimalMeta {
    public GoatMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isScreaming() {
        return get(MetadataDef.Goat.IS_SCREAMING_GOAT);
    }

    public void setScreaming(boolean screaming) {
        set(MetadataDef.Goat.IS_SCREAMING_GOAT, screaming);
    }

    public boolean hasLeftHorn() {
        return get(MetadataDef.Goat.HAS_LEFT_HORN);
    }

    public void setLeftHorn(boolean leftHorn) {
        set(MetadataDef.Goat.HAS_LEFT_HORN, leftHorn);
    }

    public boolean hasRightHorn() {
        return get(MetadataDef.Goat.HAS_RIGHT_HORN);
    }

    public void setRightHorn(boolean rightHorn) {
        set(MetadataDef.Goat.HAS_RIGHT_HORN, rightHorn);
    }
}
